package com.infamousgc.infamousCORE;

import com.infamousgc.infamousCORE.Managers.ConfigManager;
import com.infamousgc.infamousCORE.Managers.Logger;
import com.infamousgc.infamousCORE.Managers.Modules;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private ConfigManager generalConfig;

    @Override
    public void onEnable() {
        loadConfig();
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfig() {
        generalConfig = new ConfigManager(this, "config.yml");
    }

    private void registerCommands() {
        for (Modules module : Modules.values()) {
            try {
                CommandExecutor executor = module.getExecutor().getDeclaredConstructor().newInstance();
                TabCompleter tabCompleter = module.getTabCompleter().getDeclaredConstructor().newInstance();
                boolean moduleLoaded = true;

                for (String commandName : module.getCommands()) {
                    PluginCommand command = getCommand(commandName);
                    if (command != null) {
                        command.setExecutor(executor);
                        command.setTabCompleter(tabCompleter);
                    } else {
                        Logger.warning("Failed to register command '{0}' of module '{1}", commandName, module.name());
                        moduleLoaded = false;
                    }
                }

                if (moduleLoaded)
                    Logger.info("Successfully loaded module: {0}", module.name());
                else
                    Logger.warning("Module '{0}' may not work as expected. Loaded with some command registration failures", module.name());
            } catch (Exception e) {
                Logger.severe("Failed to initialize command executor for {0}: {1}", module.name(), e.getMessage());
                Logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public ConfigManager generalConfig() { return generalConfig; }
}
