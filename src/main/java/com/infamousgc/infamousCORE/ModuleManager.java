package com.infamousgc.infamousCORE;

import com.infamousgc.infamousCORE.Modules.*;
import com.infamousgc.infamousCORE.TabCompleters.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public enum ModuleManager {
    CONDENSE(CondenseModule.class, CondenseTab.class, "condense"),
    FEED(FeedModule.class, FeedHealTab.class, "feed"),
    GAMEMODE(GamemodeModule.class, GamemodeTab.class, "gamemode", "gms", "gmc", "gma", "gmsp"),
    HEAL(HealModule.class, FeedHealTab.class, "heal"),
    HOME(HomeModule.class, HomeTab.class, "sethome", "delhome", "home", "homes"),
    NICKNAME(NicknameModule.class, NicknameTab.class, "nickname"),
    SPAWN(SpawnModule.class, SpawnTab.class, "spawn", "setspawn"),
    TPA(TpaModule.class, TpaTab.class, "tpa", "tpcancel", "tpaccept", "tpdeny");

    private final Class<? extends CommandExecutor> executor;
    private final Class<? extends TabCompleter> tabCompleter;
    private final List<String> commands;
    private boolean enabled;

    ModuleManager(Class<? extends CommandExecutor> executor, Class<? extends TabCompleter> tabCompleter, String... commands) {
        this.executor = executor;
        this.tabCompleter = tabCompleter;
        this.commands = Arrays.asList(commands);
        this.enabled = true;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void enabled() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}
