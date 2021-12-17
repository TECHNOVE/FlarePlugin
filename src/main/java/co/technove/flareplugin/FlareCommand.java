package co.technove.flareplugin;

import co.technove.flare.exceptions.UserReportableException;
import co.technove.flare.internal.profiling.ProfileType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class FlareCommand implements CommandExecutor {

    private final FlarePlugin plugin;

    public FlareCommand(@NotNull final FlarePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (!command.testPermission(sender)) {
            return true;
        }

        if (this.plugin.getFlareURI().getScheme() == null) {
            sender.sendMessage(ChatColor.RED + "Invalid URL for Flare, check your config.");
            return true;
        }

        if (this.plugin.getAccessToken().length() == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid token for Flare, check your config.");
            return true;
        }

        final ProfilingManager profilingManager = this.plugin.getProfilingManager();

        if (profilingManager.isProfiling()) {
            if (profilingManager.stop()) {
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(ChatColor.GREEN + "Profiling has been stopped.");
                }
            } else {
                sender.sendMessage(ChatColor.GREEN + "Profiling has already been stopped.");
            }
        } else {
            ProfileType profileType = ProfileType.ITIMER;
            if (args.length > 0) {
                try {
                    profileType = ProfileType.valueOf(args[0].toUpperCase());
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Invalid profile type " + args[0] + "!");
                }
            }
            final ProfileType finalProfileType = profileType;
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try {
                    if (profilingManager.start(finalProfileType)) {
                        if (!(sender instanceof ConsoleCommandSender)) {
                            sender.sendMessage(ChatColor.GREEN + "Flare has been started: " + profilingManager.getProfilingUri());
                            sender.sendMessage(ChatColor.GREEN + "   Run /" + label + " to stop the Flare.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Flare has already been started!");
                    }
                } catch (UserReportableException e) {
                    sender.sendMessage("Flare failed to start: " + e.getUserError());
                    if (e.getCause() != null) {
                        this.plugin.getLogger().log(Level.WARNING, "Flare failed to start", e);
                    }
                }
            });
        }
        return true;
    }
}
