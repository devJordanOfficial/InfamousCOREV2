package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.Storage.FileManager;
import com.infamousgc.infamousCORE.Tasks.CooldownManager;
import com.infamousgc.infamousCORE.Tasks.WarmupManager;
import com.infamousgc.infamousCORE.Utils.Error;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class SpawnModule implements CommandExecutor, Listener {
    private final FileManager dataFile;
    private final CooldownManager cooldownManager;
    private final WarmupManager warmupManager;
    private Location spawnLocation;

    private static final String USAGE = "/spawn [Player]";
    private static final String PERMISSION_SPAWN = "core.spawn";
    private static final String PERMISSION_SETSPAWN = "core.setspawn";
    private static final String PERMISSION_SPAWN_OTHERS = "core.spawn.others";
    private static final String PERMISSION_COOLDOWN_BYPASS_SPAWN = "core.cooldown.bypass.spawn";

    public SpawnModule(Main plugin) {
        this.dataFile = plugin.dataFile();
        this.cooldownManager = plugin.getCooldownManager();
        this.warmupManager = plugin.getWarmupManager();
        loadSpawnLocation();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase()) {
            case "spawn" -> handleSpawn(sender, args);
            case "setspawn" -> handleSetSpawn(sender);
            default -> false;
        };
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (args.length > 1) {
            Error.invalidArguments(sender, USAGE);
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                Error.mustBePlayer(sender);
                return true;
            }

            if (!player.hasPermission(PERMISSION_SPAWN)) {
                Error.noPermission(sender, PERMISSION_SPAWN);
                return true;
            }

            teleportToSpawn(player);
        } else {
            if (!sender.hasPermission(PERMISSION_SPAWN_OTHERS)) {
                Error.noPermission(sender, PERMISSION_SPAWN_OTHERS);
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                Error.playerNotOnline(sender, args[0]);
                return true;
            }

            teleportToSpawn(target);
            sender.sendMessage(format(PREFIX_GENERAL + "&7Teleported &d{0} &7to &dSpawn", target.getName()));
        }
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Error.mustBePlayer(sender);
            return true;
        }

        if (!player.hasPermission(PERMISSION_SETSPAWN)) {
            Error.noPermission(sender, PERMISSION_SETSPAWN);
            return true;
        }

        spawnLocation = player.getLocation();
        player.getWorld().setSpawnLocation(player.getLocation());
        saveSpawnLocation();
        sender.sendMessage(format(PREFIX_GENERAL + "&7Spawn set successfully"));
        return true;
    }

    private void teleportToSpawn(Player player) {
        Location loc = getSpawnLocation(player);

        if (!cooldownManager.check(player, CooldownManager.CooldownType.SPAWN) && !player.hasPermission(PERMISSION_COOLDOWN_BYPASS_SPAWN))
            return;

        warmupManager.start(player.getUniqueId(), () -> {
            player.teleport(loc);
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacy(format("&dWhoosh &7- Teleported you to &dSpawn")));
            cooldownManager.setCooldown(player, CooldownManager.CooldownType.SPAWN);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore())
            player.teleport(getSpawnLocation(player));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn() && !event.isAnchorSpawn())
            event.setRespawnLocation(getSpawnLocation(event.getPlayer()));
    }

    private Location getSpawnLocation(Player player) {
        return spawnLocation != null ? spawnLocation : player.getWorld().getSpawnLocation();
    }

    private void loadSpawnLocation() {
        FileConfiguration data = dataFile.getConfig();
        if (data.getString("spawn.world") == null) return;
        World world = Bukkit.getWorld(data.getString("spawn.world"));
        if (world != null) {
            double x = data.getDouble("spawn.x");
            double y = data.getDouble("spawn.y");
            double z = data.getDouble("spawn.z");
            float yaw = (float) data.getDouble("spawn.yaw");
            float pitch = (float) data.getDouble("spawn.pitch");
            spawnLocation = new Location(world, x, y, z, yaw, pitch);
        }
    }

    private void saveSpawnLocation() {
        if (spawnLocation != null && spawnLocation.getWorld() != null) {
            FileConfiguration data = dataFile.getConfig();
            data.set("spawn.world", spawnLocation.getWorld().getName());
            data.set("spawn.x", spawnLocation.getX());
            data.set("spawn.y", spawnLocation.getY());
            data.set("spawn.z", spawnLocation.getZ());
            data.set("spawn.yaw", spawnLocation.getYaw());
            data.set("spawn.pitch", spawnLocation.getPitch());
            dataFile.saveConfig();
        }
    }
}
