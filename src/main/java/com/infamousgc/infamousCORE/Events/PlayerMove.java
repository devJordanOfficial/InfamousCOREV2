package com.infamousgc.infamousCORE.Events;

import com.infamousgc.infamousCORE.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class PlayerMove implements Listener {
    private final Main plugin;

    public PlayerMove(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (plugin.getWarmupManager().contains(uuid) && hasPositionChanged(event))
            plugin.getWarmupManager().cancel(uuid);
    }

    private boolean hasPositionChanged(PlayerMoveEvent event) {
        if (event.getTo() == null)
            return false;

        return event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ();
    }
}
