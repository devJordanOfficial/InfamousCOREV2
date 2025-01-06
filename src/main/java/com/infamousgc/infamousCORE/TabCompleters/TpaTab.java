package com.infamousgc.infamousCORE.TabCompleters;

import com.infamousgc.infamousCORE.Modules.TpaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TpaTab implements TabCompleter {

    private final TpaModule tpaModule;

    public TpaTab(TpaModule tpaModule) {
        this.tpaModule = tpaModule;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();

        if (args.length == 1) {
            return switch (command.getName().toLowerCase()) {
                case "tpa" -> getOnlinePlayers(args[0].toLowerCase());
                case "tpaccept", "tpdeny" -> getRequestingPlayers(player, args[0].toLowerCase());
                default -> new ArrayList<>();
            };
        }

        return new ArrayList<>();
    }

    private List<String> getOnlinePlayers(String input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> getRequestingPlayers(Player player, String input) {
        UUID targetUUID = player.getUniqueId();
        return tpaModule.getIncomingRequests(targetUUID).stream()
                .map(Bukkit::getPlayer)
                .filter(sender -> sender != null && sender.getName().toLowerCase().startsWith(input.toLowerCase()))
                .map(Player::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
