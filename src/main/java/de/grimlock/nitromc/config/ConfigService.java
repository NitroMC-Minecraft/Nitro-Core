package de.grimlock.nitromc.config;

import de.grimlock.nitromc.Main;
import de.grimlock.nitromc.service.IService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ConfigService implements IService {

    private final Main plugin;
    private final Map<String, ConfigFile> configs = new HashMap<>();

    @Inject
    public ConfigService(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        // Any global config initialization
    }

    @Override
    public void onDisable() {
        configs.values().forEach(ConfigFile::save);
    }

    public ConfigFile getConfig(String name) {
        return configs.computeIfAbsent(name, n -> new ConfigFile(plugin, n));
    }

    public void reloadConfigs() {
        configs.values().forEach(ConfigFile::reload);
    }

    public static class ConfigFile {
        private final File file;
        private FileConfiguration configuration;

        public ConfigFile(Main plugin, String name) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            this.file = new File(plugin.getDataFolder(), name + ".yml");
            if (!file.exists()) {
                try {
                    if (plugin.getResource(name + ".yml") != null) {
                        plugin.saveResource(name + ".yml", false);
                    } else {
                        file.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.configuration = YamlConfiguration.loadConfiguration(file);
        }

        public FileConfiguration get() {
            return configuration;
        }

        public void save() {
            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void reload() {
            this.configuration = YamlConfiguration.loadConfiguration(file);
        }
    }
}
