package com.infamousgc.infamousCORE;

import com.infamousgc.infamousCORE.Events.PlayerMove;
import com.infamousgc.infamousCORE.Utils.Logger;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;

public class Registrar {
    private final Main plugin;

    public Registrar(Main plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        for (ModuleManager module : ModuleManager.values()) {
            try {
                CommandExecutor executor = createExecutor(module.getExecutor());
                TabCompleter tabCompleter = createTabCompleter(module.getTabCompleter());
                boolean moduleLoaded = true;

                for (String name : module.getCommands()) {
                    PluginCommand commmand = plugin.getCommand(name);
                    if (commmand != null) {
                        commmand.setExecutor(executor);
                        commmand.setTabCompleter(tabCompleter);
                    } else {
                        Logger.warning("Failed to register command '{0}' of module '{1}'", name, module.name());
                        moduleLoaded = false;
                    }
                }

                if (moduleLoaded) Logger.info("Successfully loaded module: {0}", module.name());
                else Logger.warning("Module '{0}' may not work as expected. Loaded with command registration failures", module.name());
            } catch (Exception e) {
                Logger.severe("Failed to initialize command executor for {0}: {1}", module.name(), e.getMessage());
                Logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            }
        }

        PluginCommand confirmCommand = plugin.getCommand("confirm");
        if (confirmCommand != null)
            confirmCommand.setExecutor(plugin.getConfirmationManager());

        PluginCommand cancelCommand = plugin.getCommand("cancel");
        if (cancelCommand != null)
            cancelCommand.setExecutor(plugin.getConfirmationManager());
    }

    public void registerListeners() {
        PluginManager manager = plugin.getServer().getPluginManager();
        String listenerPackage = "com.infamousgc.infamousCORE.Events";

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(listenerPackage))
                .setScanners(Scanners.SubTypes));
        Set<Class<? extends Listener>> listenerClasses = reflections.getSubTypesOf(Listener.class);

        for (Class<? extends Listener> listenerClass : listenerClasses) {
            try {
                Listener listener = createListenerInstance(listenerClass);
                manager.registerEvents(listener, plugin);
            } catch (Exception e) {
                Logger.severe("Failed to register listener {0}: {1}", listenerClass.getSimpleName(), e.getMessage());
                Logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            }
        }
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
