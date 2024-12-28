package com.infamousgc.infamousCORE.Storage;

import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.Utils.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileManager {
    private final Main plugin;
    private final String fileName;

    private FileConfiguration ranksConfig = null;
    private File configFile = null;

    public FileManager(Main plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (configFile == null)
            configFile = new File(plugin.getDataFolder(), fileName);

        ranksConfig = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            ranksConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (ranksConfig == null)
            reloadConfig();
        return ranksConfig;
    }

    public void saveConfig() {
        if (ranksConfig == null || configFile == null)
            return;

        try {
            getConfig().save(configFile);
        } catch(IOException e) {
            Logger.severe("&7Could not save config to " + configFile);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), fileName);
        }
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
}
