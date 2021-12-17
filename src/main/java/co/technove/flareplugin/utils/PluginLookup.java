package co.technove.flareplugin.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PluginLookup implements Listener {
    private final Cache<String, String> pluginNameCache = CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .maximumSize(1024)
      .build();

    private final Map<ClassLoader, Plugin> classLoaderToPlugin = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginLoad(final PluginEnableEvent event) {
        classLoaderToPlugin.put(event.getPlugin().getClass().getClassLoader(), event.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(final PluginDisableEvent event) {
        while (classLoaderToPlugin.values().remove(event.getPlugin()));
    }

    public @NotNull Optional<String> getPluginForClass(@NotNull final String name) {
        if (name.startsWith("net.minecraft") || name.startsWith("java.") || name.startsWith("com.mojang") ||
          name.startsWith("com.google") || name.startsWith("it.unimi") || name.startsWith("sun")) {
            return Optional.empty();
        }

        final String existing = this.pluginNameCache.getIfPresent(name);
        if (existing != null) {
            return Optional.ofNullable(existing.isEmpty() ? null : existing);
        }


        final Class<?> loadedClass;
        try {
            loadedClass = Class.forName(name);
        } catch (ClassNotFoundException e) {
            this.pluginNameCache.put(name, "");
            return Optional.empty();
        }

        if (loadedClass.getClassLoader() == null) {
            this.pluginNameCache.put(name, "");
            return Optional.empty();
        }

        final Plugin plugin = this.classLoaderToPlugin.get(loadedClass.getClassLoader());
        final String pluginName = plugin == null ? "" : plugin.getName();

        this.pluginNameCache.put(name, pluginName);
        return Optional.ofNullable(pluginName.isEmpty() ? null : pluginName);
    }
}
