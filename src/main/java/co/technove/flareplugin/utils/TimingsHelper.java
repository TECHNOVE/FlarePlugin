package co.technove.flareplugin.utils;

import co.technove.flareplugin.FlarePlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

public class TimingsHelper {

    private static boolean failed = false;
    private static List<String> hiddenConfigs;

    public synchronized static @Nullable List<String> getHiddenConfigs() {
        if (failed) {
            return null;
        }

        if (hiddenConfigs == null) {
            try {
                final Class clazz = Class.forName("co.aikar.timings.TimingsManager");
                final Field hiddenConfigs = clazz.getField("hiddenConfigs");
                List list = (List) hiddenConfigs.get(null);

                TimingsHelper.hiddenConfigs = list;
            } catch (Throwable t) {
                JavaPlugin.getPlugin(FlarePlugin.class).getLogger().log(Level.WARNING, "Failed to find hidden configs, will not send config files.", t);
                failed = true;
                return null;
            }
        }

        return hiddenConfigs;
    }
}
