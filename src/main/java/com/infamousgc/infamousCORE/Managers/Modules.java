package com.infamousgc.infamousCORE.Managers;

import com.infamousgc.infamousCORE.Modules.GamemodeModule;
import com.infamousgc.infamousCORE.Modules.NicknameModule;
import com.infamousgc.infamousCORE.TabCompleters.GamemodeTab;
import com.infamousgc.infamousCORE.TabCompleters.NicknameTab;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public enum Modules {
    GAMEMODE(GamemodeModule.class, GamemodeTab.class, "gamemode", "gms", "gmc", "gma", "gmsp"),
    NICKNAME(NicknameModule.class, NicknameTab.class, "nickname");

    private final Class<? extends CommandExecutor> executor;
    private final Class<? extends TabCompleter> tabCompleter;
    private final List<String> commands;

    Modules(Class<? extends CommandExecutor> executor, Class<? extends TabCompleter> tabCompleter, String... commands) {
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
