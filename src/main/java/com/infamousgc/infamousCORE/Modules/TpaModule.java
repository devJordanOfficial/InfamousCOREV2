package com.infamousgc.infamousCORE.Modules;

import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.Tasks.CooldownManager;
import com.infamousgc.infamousCORE.Tasks.WarmupManager;
import com.infamousgc.infamousCORE.Utils.Error;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class TpaModule implements CommandExecutor {
    private final Main plugin;
    private final WarmupManager warmupManager;
    private final CooldownManager cooldownManager;
    private final Map<UUID, Map<UUID, Long>> incomingRequests = new HashMap<>(); // target -> (sender -> timestamp)
    private final Map<UUID, UUID> outgoingRequests = new HashMap<>(); // sender -> target
    private final Map<UUID, BukkitTask> expirationTasks = new HashMap<>(); // sender -> task

    private static final String TPA_USAGE = "/tpa <Player>";
    private static final String TPACCEPT_USAGE = "/tpaccept [Player]";
    private static final String TPDENY_USAGE = "/tpdeny [Player]";
    private static final String PERMISSION_TPA = "core.tpa";
    private static final String PERMISSION_COOLDOWN_BYPASS_TPA = "core.cooldown.bypass.tpa";
    private static final int REQUEST_EXPIRATION_TIME = 60; // in seconds

    public TpaModule(Main plugin) {
        this.plugin = plugin;
        this.warmupManager = plugin.getWarmupManager();
        this.cooldownManager = plugin.getCooldownManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Error.mustBePlayer(sender);
            return true;
        }

        return switch (command.getName().toLowerCase()) {
            case "tpa" -> handleTpa(player, args);
            case "tpaccept" -> handleTpaccept(player, args);
            case "tpdeny" -> handleTpdeny(player, args);
            case "tpcancel" -> handleTpcancel(player);
            default -> false;
        };
    }

    private boolean handleTpa(Player sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_TPA)) {
            Error.noPermission(sender, PERMISSION_TPA);
            return true;
        }

        if (args.length != 1) {
            Error.invalidArguments(sender, TPA_USAGE);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Error.playerNotOnline(sender, args[0]);
            return true;
        }

        if (target.equals(sender)) {
            sender.sendMessage(format(PREFIX_ERROR + "&7You cannot send a teleport request to yourself"));
            return true;
        }

        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (outgoingRequests.containsKey(senderUUID)) {
            sender.sendMessage(format(PREFIX_INFO + "&7You already have an outgoing teleport request. You must cancel it first with &e/tpcancel"));
            return true;
        }

        if (!cooldownManager.check(sender, CooldownManager.CooldownType.TPA) && !sender.hasPermission(PERMISSION_COOLDOWN_BYPASS_TPA))
            return true;

        outgoingRequests.put(senderUUID, targetUUID);
        incomingRequests.computeIfAbsent(targetUUID, k -> new HashMap<>()).put(senderUUID, System.currentTimeMillis());

        sender.sendMessage(format(PREFIX_GENERAL + "&7Teleport request sent to &d{0}", target.getName()));
        target.sendMessage(format(PREFIX_GENERAL +
                "&d{0} &7has requested to teleport to you. Type &d/tpaccept &7to accept or &d/tpdeny &7to deny", sender.getName()));

        scheduleExpirationTask(senderUUID, sender.getName(), targetUUID, target.getName());

        return true;
    }

    private boolean handleTpaccept(Player target, String[] args) {
        UUID targetUUID = target.getUniqueId();

        if (args.length > 1) {
            Error.invalidArguments(target, TPACCEPT_USAGE);
            return true;
        }

        Map<UUID, Long> requests = incomingRequests.get(targetUUID);

        UUID senderUUID = getSenderUUID(target, args, requests);
        if (senderUUID == null) return true;

        Player sender = Bukkit.getPlayer(senderUUID);
        if (sender == null || !sender.isOnline()) {
            target.sendMessage(format(PREFIX_INFO + "&7The player who sent the request is no longer online"));
            cleanupRequest(senderUUID, targetUUID);
            return true;
        }

        target.sendMessage(format(PREFIX_GENERAL + "&7You accepted &d{0}&7's teleport request", sender.getName()));
        sender.sendMessage(format(PREFIX_GENERAL + "&d{0} &7accepted your teleport request", target.getName()));

        warmupManager.start(senderUUID, () -> {
            if (sender.isOnline() && target.isOnline()) {
                sender.teleport(target.getLocation());
                sender.sendMessage(format(PREFIX_GENERAL + "&7Teleported to &d{0}", target.getName()));
                cooldownManager.setCooldown(sender, CooldownManager.CooldownType.TPA);
            }
        });

        cleanupRequest(senderUUID, targetUUID);
        return true;
    }

    private boolean handleTpdeny(Player target, String[] args) {
        UUID targetUUID = target.getUniqueId();

        if (args.length > 1) {
            Error.invalidArguments(target, TPDENY_USAGE);
            return true;
        }

        Map<UUID, Long> requests = incomingRequests.get(targetUUID);

        UUID senderUUID = getSenderUUID(target, args, requests);
        if (senderUUID == null) return true;

        Player sender = Bukkit.getPlayer(senderUUID);
        target.sendMessage(format(PREFIX_GENERAL + "&7Teleport request denied"));
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(format(PREFIX_GENERAL + "&d{0} &7declined your teleport request", target.getName()));
        }

        cleanupRequest(senderUUID, targetUUID);
        return true;
    }

    private boolean handleTpcancel(Player sender) {
        if (!sender.hasPermission(PERMISSION_TPA)) {
            Error.noPermission(sender, PERMISSION_TPA);
            return true;
        }

        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = outgoingRequests.get(senderUUID);

        if (targetUUID == null) {
            sender.sendMessage(format(PREFIX_INFO + "&7You have no outgoing teleport requests"));
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);

        cleanupRequest(senderUUID, targetUUID);

        sender.sendMessage(format(PREFIX_GENERAL + "&7Teleport request{0} &7has been cancelled",
                target != null ? " to &d" + target.getName() : ""));

        if (target != null && target.isOnline()) {
            target.sendMessage(format(PREFIX_GENERAL + "&d{0} &7cancelled their teleport request", sender.getName()));
        }

        return true;
    }

    private void cleanupRequest(UUID sender, UUID target) {
        outgoingRequests.remove(sender);
        incomingRequests.computeIfPresent(target, (k, v) -> {
            v.remove(sender);
            return v.isEmpty() ? null : v;
        });

        Optional.ofNullable(expirationTasks.remove(sender)).ifPresent(BukkitTask::cancel);
    }

    private UUID getSenderUUID(Player target, String[] args, Map<UUID, Long> requests) {
        if (requests == null || requests.isEmpty()) {
            target.sendMessage(format(PREFIX_INFO + "&7You have no pending teleport requests"));
            return null;
        }

        if (args.length > 0) {
            Player sender = Bukkit.getPlayer(args[0]);
            if (sender == null) {
                target.sendMessage(format(PREFIX_INFO + "&e{0} &7is not online", args[0]));
                return null;
            }
            UUID senderUUID = sender.getUniqueId();
            if (!requests.containsKey(senderUUID)) {
                target.sendMessage(format(PREFIX_INFO + "&7You have no pending requests from &e{0}", args[0]));
                return null;
            }
            return senderUUID;
        } else {
            return requests.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
    }

    private void scheduleExpirationTask(UUID senderUUID, String senderName, UUID targetUUID, String targetName) {
        BukkitTask expirationTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (outgoingRequests.remove(senderUUID, targetUUID)) {
                incomingRequests.getOrDefault(targetUUID, new HashMap<>()).remove(senderUUID);
                Player sender = Bukkit.getPlayer(senderUUID);
                Player target = Bukkit.getPlayer(targetUUID);
                if (sender != null)
                    sender.sendMessage(format(PREFIX_INFO + "&7Your teleport request to &e{0} &7has expired", targetName));
                if (target != null)
                    target.sendMessage(format(PREFIX_INFO + "&7The teleport request from &e{0} &7has expired", senderName));
            }
        }, REQUEST_EXPIRATION_TIME * 20);

        expirationTasks.put(senderUUID, expirationTask);
    }

    public List<UUID> getIncomingRequests(UUID targetUUID) {
        Map<UUID, Long> requests = incomingRequests.get(targetUUID);
        return requests != null ? new ArrayList<>(requests.keySet()) : new ArrayList<>();
    }
}
