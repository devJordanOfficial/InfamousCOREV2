package com.infamousgc.infamousCORE.TabCompleters;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GamemodeTab implements TabCompleter {
    private static final List<String> SHORTHAND_COMMANDS = Arrays.asList("gms", "gmc", "gma", "gmsp");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (SHORTHAND_COMMANDS.contains(command.getName().toLowerCase()))
            return args.length == 1 ? getPlayerSuggestions(args[0]) : new ArrayList<>();

        if (command.getName().equalsIgnoreCase("gamemode")) {
            if (args.length == 1) return getGamemodeSuggestions(args[0]);
            else if (args.length == 2) return getPlayerSuggestions(args[1]);
        }

        return new ArrayList<>();
    }

    private List<String> getGamemodeSuggestions(String input) {
        return Stream.of("survival", "creative", "adventure", "spectator")
                .filter(mode -> mode.startsWith(input))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> getPlayerSuggestions(String input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}
