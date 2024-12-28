package com.infamousgc.infamousCORE.TabCompleters;

import com.infamousgc.infamousCORE.Data.PlayerData;
import com.infamousgc.infamousCORE.Data.PlayerDataManager;
import com.infamousgc.infamousCORE.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HomeTab implements TabCompleter {
    private final PlayerDataManager playerDataManager;

    private static final String PERMISSION_HOME_OTHERS = "core.home.others";

    public HomeTab(Main plugin) {
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        return switch (command.getName().toLowerCase()) {
            case "home" -> handleHome(player, args);
            case "delhome" -> handleDelHome(player, args);
            case "homes" -> handleHomes(player, args);
            default -> Collections.emptyList();
        };
    }

    private List<String> handleHome(Player player, String[] args) {
        if (args.length != 1) return Collections.emptyList();

        if (args[0].contains(":") && player.hasPermission(PERMISSION_HOME_OTHERS)) {
            String[] parts = args[0].split(":", 2);
            String username = parts[0];
            String homeName = parts.length > 1 ? parts[1] : "";

            Player target = Bukkit.getPlayer(username);
            if (target != null) {
                return getPlayerHomes(target).stream()
                        .map(home -> username + ":" + home)
                        .filter(home -> home.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
        }

        return getPlayerHomes(player).stream()
                .filter(home -> home.startsWith(args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> handleDelHome(Player player, String[] args) {
        if (args.length != 1) return Collections.emptyList();

        return getPlayerHomes(player).stream()
                .filter(home -> home.startsWith(args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> handleHomes(Player player, String[] args) {
        if (args.length != 1 || !player.hasPermission(PERMISSION_HOME_OTHERS)) return Collections.emptyList();

        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.startsWith(args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> getPlayerHomes(Player player) {
        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        return new ArrayList<>(data.getHomes().keySet());
    }
}
