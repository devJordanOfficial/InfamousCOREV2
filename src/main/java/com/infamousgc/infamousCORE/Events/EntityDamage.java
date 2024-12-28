package com.infamousgc.infamousCORE.Events;

import com.infamousgc.infamousCORE.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class EntityDamage implements Listener {
    private final Main plugin;

    public EntityDamage(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (plugin.getWarmupManager().contains(uuid))
            plugin.getWarmupManager().cancel(uuid);
    }
}
