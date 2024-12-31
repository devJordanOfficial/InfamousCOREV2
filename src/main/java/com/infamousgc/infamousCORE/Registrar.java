package com.infamousgc.infamousCORE;

import com.infamousgc.infamousCORE.Events.EntityDamage;
import com.infamousgc.infamousCORE.Events.PlayerMove;
import com.infamousgc.infamousCORE.Utils.Logger;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Registrar {
    private final Main plugin;

    public Registrar(Main plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        int success = 0;
        List<String> failed = new ArrayList<>();
        for (ModuleManager module : ModuleManager.values()) {
            if (!module.isEnabled())
                Logger.warning("Module '{0}' is disabled: No MySQL connection found", module.name());

            try {
                CommandExecutor executor = createExecutor(module.getExecutor());
                TabCompleter tabCompleter = createTabCompleter(module.getTabCompleter());
                boolean moduleLoaded = true;

                for (String name : module.getCommands()) {
                    PluginCommand command = plugin.getCommand(name);
                    if (command != null) {
                        command.setExecutor(executor);
                        if (module.isEnabled())
                            command.setTabCompleter(tabCompleter);
                    } else {
                        Logger.warning("Failed to register command '{0}' of module '{1}'", name, module.name());
                        moduleLoaded = false;
                    }
                }

                if (moduleLoaded) success++;
                else {
                    failed.add(module.name());
                    Logger.warning("Module '{0}' may not work as expected. Loaded with command registration failures", module.name());
                }
            } catch (Exception e) {
                failed.add(module.name());
                Logger.severe("Failed to initialize command executor for {0}: {1}", module.name(), e.getMessage());
                Logger.printStackTrace(e.getStackTrace());
            }
        }

        Logger.info("Loaded {0} modules", success);

        if (failed.isEmpty()) {
            Logger.info("All modules loaded successfully");
        } else {
            Logger.warning("Failed to load {0} modules", failed.size());
            failed.forEach(s -> Logger.warning("Failed to load module '{0}'", s));
        }

        // Register confirm and cancel commands
        PluginCommand confirmCommand = plugin.getCommand("confirm");
        if (confirmCommand != null)
            confirmCommand.setExecutor(plugin.getConfirmationManager());

        PluginCommand cancelCommand = plugin.getCommand("cancel");
        if (cancelCommand != null)
            cancelCommand.setExecutor(plugin.getConfirmationManager());
    }

    public void registerListeners() {
        PluginManager manager = plugin.getServer().getPluginManager();
        manager.registerEvents(new EntityDamage(plugin), plugin);
        manager.registerEvents(new PlayerMove(plugin), plugin);
    }

    private CommandExecutor createExecutor(Class<? extends CommandExecutor> executorClass) throws Exception {
        return createInstance(executorClass);
    }

    private TabCompleter createTabCompleter(Class<? extends TabCompleter> tabCompleterClass) throws Exception {
        return createInstance(tabCompleterClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0] == Main.class) {
                return (T) constructor.newInstance(plugin);
            }
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Listener createListenerInstance(Class<? extends Listener> listenerClass) throws Exception {
        Constructor<?>[] constructors = listenerClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0] == Main.class)
                return (Listener) constructor.newInstance(plugin);
        }
        return listenerClass.getDeclaredConstructor().newInstance();
    }
}
