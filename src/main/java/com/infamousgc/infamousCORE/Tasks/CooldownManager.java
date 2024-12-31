package com.infamousgc.infamousCORE.Tasks;

import com.infamousgc.infamousCORE.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class CooldownManager {
    private final Main plugin;
    private final Map<CooldownType, Map<UUID, Long>> cooldowns = new EnumMap<>(CooldownType.class);

    public CooldownManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean check(Player player, CooldownType type) {
        String cooldownPath = "cooldown." + type.name().toLowerCase();
        int cooldownTime = plugin.generalConfig().getConfig().getInt(cooldownPath);
        if (cooldownTime <= 0 ) return true;

        UUID uuid = player.getUniqueId();
        Map<UUID, Long> typeCooldowns = cooldowns.computeIfAbsent(type, k -> new HashMap<>());
        long lastUsage = typeCooldowns.getOrDefault(uuid, 0L);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUsage < cooldownTime * 1000L) {
            long remainingTime = (cooldownTime * 1000L - (currentTime - lastUsage)) / 1000L;
            String message = getMessage(type, remainingTime);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(format(message)));
            return false;
        }

        return true;
    }

    public void setCooldown(Player player, CooldownType type) {
        cooldowns.computeIfAbsent(type, k -> new HashMap<>())
                .put(player.getUniqueId(), System.currentTimeMillis());
    }

    private String getMessage(CooldownType type, long remainingTime) {
        return switch (type) {
            case TELEPORT -> "&7You must wait &e" + remainingTime + " seconds&7 before teleporting again";
            case HEAL -> "&7You must wait &e" + remainingTime + " seconds&7 before healing again";
            case FEED -> "&7You must wait &e" + remainingTime + " seconds&7 before feeding yourself again";
        };
    }

    public enum CooldownType {
        TELEPORT,
        HEAL,
        FEED,
    }
}
