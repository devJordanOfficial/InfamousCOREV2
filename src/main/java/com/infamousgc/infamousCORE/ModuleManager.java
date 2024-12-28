package com.infamousgc.infamousCORE;

import com.infamousgc.infamousCORE.Modules.GamemodeModule;
import com.infamousgc.infamousCORE.Modules.HomeModule;
import com.infamousgc.infamousCORE.Modules.NicknameModule;
import com.infamousgc.infamousCORE.TabCompleters.GamemodeTab;
import com.infamousgc.infamousCORE.TabCompleters.HomeTab;
import com.infamousgc.infamousCORE.TabCompleters.NicknameTab;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public enum ModuleManager {
    GAMEMODE(GamemodeModule.class, GamemodeTab.class, "gamemode", "gms", "gmc", "gma", "gmsp"),
    HOME(HomeModule.class, HomeTab.class, "sethome", "delhome", "home", "homes"),
    NICKNAME(NicknameModule.class, NicknameTab.class, "nickname");

    private final Class<? extends CommandExecutor> executor;
    private final Class<? extends TabCompleter> tabCompleter;
    private final List<String> commands;

    ModuleManager(Class<? extends CommandExecutor> executor, Class<? extends TabCompleter> tabCompleter, String... commands) {
        this.executor = executor;
        this.tabCompleter = tabCompleter;
        this.commands = Arrays.asList(commands);
    }

    public Class<? extends CommandExecutor> getExecutor() {
        return executor;
    }

    public Class<? extends TabCompleter> getTabCompleter() {
        return tabCompleter;
    }

    public List<String> getCommands() {
        return commands;
    }
}
