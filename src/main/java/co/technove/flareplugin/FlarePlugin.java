package co.technove.flareplugin;

import co.technove.flare.FlareInitializer;
import co.technove.flare.internal.profiling.InitializationException;
import co.technove.flareplugin.utils.NMSHelper;
import co.technove.flareplugin.utils.PluginLookup;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;

public class FlarePlugin extends JavaPlugin {

    private ProfilingManager profilingManager;
    private PluginLookup pluginLookup;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        try {
            NMSHelper.initialize();
        } catch (ReflectiveOperationException e) {
            this.getLogger().log(Level.WARNING, "Failed to initialize NMS, you may not be running Spigot.", e);
        }

        try {
            final List<String> warnings = FlareInitializer.initialize();
            this.getLogger().log(Level.WARNING, "Warnings while initializing Flare: " + String.join(", ", warnings));
        } catch (InitializationException e) {
            this.getLogger().log(Level.SEVERE, "Failed to initialize Flare", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.profilingManager = new ProfilingManager(this);

        this.pluginLookup = new PluginLookup();
        this.getServer().getPluginManager().registerEvents(this.pluginLookup, this);

        this.getCommand("flare").setExecutor(new FlareCommand(this));
    }

    @Override
    public void onDisable() {
        if (this.profilingManager.isProfiling()) {
            this.profilingManager.stop();
        }
    }

    public URI getFlareURI() {
        return URI.create(this.getConfig().getString("flare.url", ""));
    }

    public String getAccessToken() {
        return this.getConfig().getString("flare.token");
    }

    public ProfilingManager getProfilingManager() {
        return profilingManager;
    }

    public PluginLookup getPluginLookup() {
        return pluginLookup;
    }
}
