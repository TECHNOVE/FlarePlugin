package co.technove.flareplugin.utils;

import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ServerConfigurations {

    public static final String[] configurationFiles = new String[]{
      "server.properties",
      "bukkit.yml",
      "spigot.yml",
      "paper.yml",
      "airplane.yml",
      "purpur.yml"
    };

    public static @NotNull Map<String, String> getCleanCopies() throws IOException {
        final Map<String, String> files = new HashMap<>(configurationFiles.length);
        for (final String file : configurationFiles) {
            final String cleanCopy = getCleanCopy(file);
            if (cleanCopy == null) {
                continue;
            }
            files.put(file, cleanCopy);
        }
        return files;
    }

    public static @Nullable String getCleanCopy(@NotNull final String configName) throws IOException {
        final File file = new File(configName);
        if (!file.exists()) {
            return null;
        }

        final List<String> hiddenConfigs = TimingsHelper.getHiddenConfigs();

        if (hiddenConfigs == null) {
            return null;
        }

        switch (Files.getFileExtension(configName)) {
            case "properties": {
                final Properties properties = new Properties();
                try (final FileInputStream inputStream = new FileInputStream(file)) {
                    properties.load(inputStream);
                }
                for (final String hiddenConfig : hiddenConfigs) {
                    properties.remove(hiddenConfig);
                }
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                properties.store(outputStream, "");
                return Arrays.stream(outputStream.toString()
                  .split("\n"))
                  .filter(line -> !line.startsWith("#"))
                  .collect(Collectors.joining("\n"));
            }
            case "yml": {
                final YamlConfiguration configuration = new YamlConfiguration();
                try {
                    configuration.load(file);
                } catch (final InvalidConfigurationException e) {
                    throw new IOException(e);
                }
                configuration.options().header(null);
                for (final String key : configuration.getKeys(true)) {
                    if (hiddenConfigs.contains(key)) {
                        configuration.set(key, null);
                    }
                }
                return configuration.saveToString();
            }
            default:
                throw new IllegalArgumentException("Bad file type " + configName);
        }
    }

}
