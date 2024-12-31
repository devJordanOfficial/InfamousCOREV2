package com.infamousgc.infamousCORE.Tasks;

import com.infamousgc.infamousCORE.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class WarmupManager {
    private final Main plugin;
    private final Map<UUID, BukkitRunnable> activeWarmups = new HashMap<>();
    private static final String PERMISSION_WARMUP_BYPASS = "core.warmup.bypass";

    public WarmupManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start(UUID uuid, Runnable onComplete) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        int warmupTime = plugin.generalConfig().getConfig().getInt("warmup.teleport");
        if (warmupTime <= 0 || player.hasPermission(PERMISSION_WARMUP_BYPASS)) {
            onComplete.run();
            return;
        }

        cancel(uuid); // TODO: Don't send action bar if cancelled by another command (ie. during current warmup)

        BukkitRunnable runnable = new BukkitRunnable() {
            int remaining = warmupTime;

            @Override
            public void run() {
                if (!player.isOnline() || remaining <= 0) {
                    cancel();
                    activeWarmups.remove(uuid);
                    if (remaining <= 0 && player.isOnline()) onComplete.run();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(format("&7Teleporting in &d{0}&7...", remaining)));
                remaining--;
            }
        };

        runnable.runTaskTimer(plugin, 0L, 20L);
        activeWarmups.put(uuid, runnable);
    }

    public void cancel(UUID uuid) {
        BukkitRunnable warmupTask = activeWarmups.remove(uuid);
        if (warmupTask != null) {
            warmupTask.cancel();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(format("&cTeleportation cancelled")));
        }
    }

    public boolean contains(UUID uuid) {
        return activeWarmups.containsKey(uuid);
    }
}
