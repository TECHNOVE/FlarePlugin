package co.technove.flareplugin;

import co.technove.flare.Flare;
import co.technove.flare.FlareAuth;
import co.technove.flare.FlareBuilder;
import co.technove.flare.exceptions.UserReportableException;
import co.technove.flare.internal.profiling.ProfileType;
import co.technove.flareplugin.collectors.GCEventCollector;
import co.technove.flareplugin.collectors.StatCollector;
import co.technove.flareplugin.collectors.TPSCollector;
import co.technove.flareplugin.utils.ServerConfigurations;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.software.os.OperatingSystem;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;

// yuck
public class ProfilingManager {

    private final FlarePlugin plugin;

    private @Nullable Flare currentFlare;
    private @Nullable BukkitTask currentTask = null;

    public ProfilingManager(@NotNull final FlarePlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized boolean isProfiling() {
        return currentFlare != null && currentFlare.isRunning();
    }

    public synchronized String getProfilingUri() {
        return Objects.requireNonNull(currentFlare).getURI().map(URI::toString).orElse("Flare is not running");
    }

    public synchronized boolean start(ProfileType profileType) throws UserReportableException {
        if (currentFlare != null && !currentFlare.isRunning()) {
            currentFlare = null; // errored out
        }
        if (isProfiling()) {
            return false;
        }
        if (Bukkit.isPrimaryThread()) {
            throw new UserReportableException("Profiles should be started off-thread");
        }

        try {
            OperatingSystem os = new SystemInfo().getOperatingSystem();

            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hardware = systemInfo.getHardware();

            CentralProcessor processor = hardware.getProcessor();
            CentralProcessor.ProcessorIdentifier processorIdentifier = processor.getProcessorIdentifier();

            GlobalMemory memory = hardware.getMemory();
            VirtualMemory virtualMemory = memory.getVirtualMemory();

            FlareBuilder builder = new FlareBuilder()
                    .withProfileType(profileType)
                    .withMemoryProfiling(true)
                    .withAuth(FlareAuth.fromTokenAndUrl(this.plugin.getAccessToken(), this.plugin.getFlareURI()))

                    .withFiles(ServerConfigurations.getCleanCopies())
                    .withVersion("Primary Version", Bukkit.getVersion())
                    .withVersion("Bukkit Version", Bukkit.getBukkitVersion())

                    .withGraphCategories(CustomCategories.MC_PERF)
                    .withCollectors(new TPSCollector(), new GCEventCollector(), new StatCollector())
                    .withClassIdentifier(this.plugin.getPluginLookup()::getPluginForClass)

                    .withHardware(new FlareBuilder.HardwareBuilder()
                            .setCoreCount(processor.getPhysicalProcessorCount())
                            .setThreadCount(processor.getLogicalProcessorCount())
                            .setCpuModel(processorIdentifier.getName())
                            .setCpuFrequency(processor.getMaxFreq())

                            .setTotalMemory(memory.getTotal())
                            .setTotalSwap(virtualMemory.getSwapTotal())
                            .setTotalVirtual(virtualMemory.getVirtualMax())
                    )

                    .withOperatingSystem(new FlareBuilder.OperatingSystemBuilder()
                            .setManufacturer(os.getManufacturer())
                            .setFamily(os.getFamily())
                            .setVersion(os.getVersionInfo().toString())
                            .setBitness(os.getBitness())
                    );

            currentFlare = builder.build();
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to read configuration files:", e);
            throw new UserReportableException("Failed to load configuration files, check logs for further details.");
        }

        try {
            currentFlare.start();
        } catch (IllegalStateException e) {
            this.plugin.getLogger().log(Level.WARNING, "Error starting Flare:", e);
            throw new UserReportableException("Failed to start Flare, check logs for further details.");
        }

        currentTask = Bukkit.getScheduler().runTaskLater(plugin, this::stop, 20 * 60 * 15);
        this.plugin.getLogger().log(Level.INFO, "Flare has been started: " + getProfilingUri());
        return true;
    }

    public synchronized boolean stop() {
        if (!isProfiling()) {
            return false;
        }
        if (!currentFlare.isRunning()) {
            currentFlare = null;
            return true;
        }
        this.plugin.getLogger().log(Level.INFO, "Flare has been stopped: " + getProfilingUri());
        try {
            currentFlare.stop();
        } catch (IllegalStateException e) {
            this.plugin.getLogger().log(Level.WARNING, "Error occurred stopping Flare", e);
        }
        currentFlare = null;

        try {
            currentTask.cancel();
        } catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "Error occurred stopping Flare", t);
        }

        currentTask = null;
        return true;
    }

}
