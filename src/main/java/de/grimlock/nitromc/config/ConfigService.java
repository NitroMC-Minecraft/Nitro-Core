package de.grimlock.nitromc.config;

import de.grimlock.nitromc.Main;
import de.grimlock.nitromc.service.IService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

@Singleton
public class ConfigService implements IService {

    private final Main plugin;
    private final Map<String, ConfigFile> configs = new ConcurrentHashMap<>();
    private final List<Runnable> reloadListeners = new CopyOnWriteArrayList<>();

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
        reloadListeners.forEach(Runnable::run);
    }

    public void addReloadListener(Runnable listener) {
        reloadListeners.add(listener);
    }

    public String getString(String configName, String path) {
        return getConfig(configName).get().getString(path);
    }

    public String getString(String configName, String path, String def) {
        return getConfig(configName).get().getString(path, def);
    }

    public int getInt(String configName, String path) {
        return getConfig(configName).get().getInt(path);
    }

    public int getInt(String configName, String path, int def) {
        return getConfig(configName).get().getInt(path, def);
    }

    public boolean getBoolean(String configName, String path) {
        return getConfig(configName).get().getBoolean(path);
    }

    public boolean getBoolean(String configName, String path, boolean def) {
        return getConfig(configName).get().getBoolean(path, def);
    }

    public List<String> getStringList(String configName, String path) {
        return getConfig(configName).get().getStringList(path);
    }

    public static class ConfigFile {
        private final Main plugin;
        private final File file;
        private FileConfiguration configuration;

        public ConfigFile(Main plugin, String name) {
            this.plugin = plugin;
            file = new File(plugin.getDataFolder(), name + ".yml");

            file.getParentFile().mkdirs();

            if (!file.exists()) {
                try {
                    if (plugin.getResource(name + ".yml") != null) {
                        plugin.saveResource(name + ".yml", false);
                    } else {
                        file.createNewFile();
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create config file: " + name, e);
                }
            }

            this.configuration = YamlConfiguration.loadConfiguration(file);
            loadDefaults(name);
        }

        private void loadDefaults(String name) {
            try {
                var defaultStream = plugin.getResource(name + ".yml");
                if (defaultStream != null) {
                    YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                    configuration.setDefaults(defaults);
                    configuration.options().copyDefaults(true);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load defaults for config: " + name, e);
            }
        }

        public FileConfiguration get() {
            return configuration;
        }

        public void save() {
            try {
                configuration.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save config file: " + file.getName(), e);
            }
        }

        public void reload() {
            this.configuration = YamlConfiguration.loadConfiguration(file);
            loadDefaults(file.getName().replace(".yml", ""));
        }
    }
}
