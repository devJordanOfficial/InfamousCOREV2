package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Data.PlayerDataManager;
import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.ModuleManager;
import com.infamousgc.infamousCORE.Tasks.ConfirmationManager;
import com.infamousgc.infamousCORE.Tasks.CooldownManager;
import com.infamousgc.infamousCORE.Tasks.WarmupManager;
import com.infamousgc.infamousCORE.Utils.Error;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class HomeModule implements CommandExecutor {
    private final Main plugin;
    private final PlayerDataManager playerDataManager;
    private final ConfirmationManager confirmationManager;
    private final CooldownManager cooldownManager;
    private final WarmupManager warmupManager;

    private static final String USAGE_DELHOME = "/delhome <Name>";
    private static final String PERMISSION_HOME = "core.home";
    private static final String PERMISSION_HOME_UNLIMITED = "core.home.max.unlimited";
    private static final String PERMISSION_HOME_OTHERS = "core.home.others";
    private static final String PERMISSION_COOLDOWN_BYPASS_HOME = "core.cooldown.bypass.home";

    public HomeModule(Main plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.confirmationManager = plugin.getConfirmationManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.warmupManager = plugin.getWarmupManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!ModuleManager.HOME.isEnabled()) {
            Error.moduleDisabled(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            Error.mustBePlayer(sender);
            return true;
        }

        switch (label.toLowerCase()) {
            case "sethome" -> handleSetHome(player, args);
            case "delhome" -> handleDelHome(player, args);
            case "home" -> handleTeleport(player, args);
            case "homes" -> listHomes(player, args.length > 0 ? args[0] : null);
        }

        return true;
    }

    private void handleSetHome(Player player, String[] args) {
        if (!player.hasPermission(PERMISSION_HOME)) {
            Error.noPermission(player, PERMISSION_HOME);
            return;
        }

        String homeName = args.length > 0 ? args[0].toLowerCase() : "home";

        if (homeName.contains(":")) {
            player.sendMessage(format(PREFIX_ERROR + "&7Home names cannot include '&c:&7'"));
            return;
        }

        if (isWorldRestricted(player.getWorld().getName())) {
            player.sendMessage(format(PREFIX_ERROR + "&7You cannot set homes in this world"));
            return;
        }

        Map<String, Location> homes = playerDataManager.getPlayerData(player.getUniqueId()).getHomes();

        int max = getMaxHomes(player);
        if (homes.size() >= max && !homes.containsKey(homeName)) {
            player.sendMessage(format(PREFIX_INFO + "&7You have reached your maximum number of homes ({0})", max));
            return;
        }

        if (homes.containsKey(homeName)) {
            confirmationManager.requestConfirmation(player, format("Home '&e" + homeName + "&7' already exists. To replace it"),
                    p -> setHome(p, homeName));
        } else {
            setHome(player, homeName);
        }
    }

    private void setHome(Player player, String homeName) {
        playerDataManager.getPlayerData(player.getUniqueId()).setHome(homeName, player.getLocation());
        player.sendMessage(format(PREFIX_GENERAL + "&7Your home '&d{0}&7' has been set", homeName));
    }

    private void handleDelHome(Player player, String[] args) {
        if (args.length != 1) {
            Error.invalidArguments(player, USAGE_DELHOME);
            return;
        }

        String homeName = args[0].toLowerCase();
        UUID uuid = player.getUniqueId();
        Map<String, Location> homes = playerDataManager.getPlayerData(uuid).getHomes();

        if (!homes.containsKey(homeName)) {
            player.sendMessage(format(PREFIX_ERROR + "&7Home '&c{0}&7' does not exist", homeName));
            return;
        }

        playerDataManager.getPlayerData(uuid).deleteHome(homeName);
        player.sendMessage(format(PREFIX_GENERAL + "&7Home '&d{0}&7' has been deleted", homeName));
    }

    private void handleTeleport(Player player, String[] args) {
        String input = args.length > 0 ? args[0].toLowerCase() : "home";
        UUID uuid = player.getUniqueId();
        String homeName;

        if (input.contains(":")) {
            if (!player.hasPermission(PERMISSION_HOME_OTHERS)) {
                Error.noPermission(player, PERMISSION_HOME_OTHERS);
                return;
            }
            String[] parts = input.split(":", 2);
            Player target = Bukkit.getPlayer(parts[0]);
            if (target == null) {
                player.sendMessage(format(PREFIX_ERROR + "&7Player '&c{0}&7' is not online", parts[0]));
                return;
            }
            uuid = target.getUniqueId();
            homeName = parts.length > 1 ? parts[1].toLowerCase() : "home";
        } else {
            homeName = input;
        }

        Map<String, Location> homes = playerDataManager.getPlayerData(uuid).getHomes();

        if (homes.isEmpty()) {
            if (player.getUniqueId() == uuid)
                player.sendMessage(format(PREFIX_INFO + "&7{0} no homes",
                        player.getUniqueId() == uuid ? "You have" : Bukkit.getPlayer(uuid).getName() + " has"));
            return;
        }

        if (!homes.containsKey(homeName)) {
            player.sendMessage(format(PREFIX_ERROR + "&7Home '&c{0}&7' does not exist", homeName));
            return;
        }

        Location loc = homes.get(homeName);
        if (loc == null || loc.getWorld() == null) {
            player.sendMessage(format(PREFIX_ERROR + "&7The location for home '&c{0}&7' no longer exists", homeName));
            confirmationManager.requestConfirmation(player, "remove the invalid home '" + homeName + "'",
                    p -> playerDataManager.getPlayerData(p.getUniqueId()).deleteHome(homeName));
            return;
        }

        if (!cooldownManager.check(player, CooldownManager.CooldownType.HOME) && !player.hasPermission(PERMISSION_COOLDOWN_BYPASS_HOME))
            return;

        warmupManager.start(uuid, () -> {
            player.teleport(loc);
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacy(format("&dWhoosh &7- Teleported you to &d{0}", homeName)));
            cooldownManager.setCooldown(player, CooldownManager.CooldownType.HOME);
        });
    }

    private void listHomes(Player player, String username) {
        UUID uuid = player.getUniqueId();
        if (username != null) {
            if (!player.hasPermission(PERMISSION_HOME_OTHERS)) {
                Error.noPermission(player, PERMISSION_HOME_OTHERS);
                return;
            }
            Player target = Bukkit.getPlayer(username);
            if (target == null) {
                player.sendMessage(format(PREFIX_ERROR + "&7Player '&c{0}&7' is not online", username));
                return;
            }
            uuid = target.getUniqueId();
        }

        Map<String, Location> homes = playerDataManager.getPlayerData(uuid).getHomes();

        if (homes.isEmpty()) {
            if (player.getUniqueId() == uuid)
                player.sendMessage(format(PREFIX_INFO + "&7{0} no homes", player.getUniqueId() == uuid ? "You have" : username + " has"));
            return;
        }

        player.sendMessage(format(PREFIX_GENERAL + "&7Homes:"));

        TextComponent message = new TextComponent();
        List<String> homeList = homes.keySet().stream().sorted().toList();
        for (int i = 0; i < homeList.size(); i++) {
            String home = homeList.get(i);
            TextComponent indent = new TextComponent(format("&7- "));
            TextComponent clickable = new TextComponent(format("&d" + home.substring(0, 1).toUpperCase() + home.substring(1)));
            clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + home));
            clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(format("&7Click to teleport to &d{0}", home.substring(0, 1).toUpperCase() + home.substring(1))).create()));

            message.addExtra(indent); message.addExtra(clickable);

            if (i < homeList.size() - 1) message.addExtra("\n");
        }

        player.spigot().sendMessage(message);
    }

    private int getMaxHomes(Player player) {
        if (player.hasPermission(PERMISSION_HOME_UNLIMITED)) return Integer.MAX_VALUE;

        int max = plugin.generalConfig().getConfig().getInt("home.max-permission-check");
        for (int i = max; i > 0; i--) {
            if (player.hasPermission("core.home.max." + i)) return i;
        }

        return 1;
    }

    private boolean isWorldRestricted(String world) {
        return plugin.generalConfig().getConfig().getStringList("home.restricted-worlds").contains(world);
    }
}
