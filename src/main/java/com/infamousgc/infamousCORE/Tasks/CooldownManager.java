package com.infamousgc.infamousCORE.Tasks;

import com.infamousgc.infamousCORE.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.infamousgc.infamousCORE.Utils.Formatter.*;

public class CooldownManager {
    private final Main plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final String PERMISSION_COOLDOWN_BYPASS = "core.cooldown.bypass";

    public CooldownManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean check(Player player) {
        if (player.hasPermission(PERMISSION_COOLDOWN_BYPASS)) return true;

        String cooldownPath = "teleport-cooldown";
        int cooldownTime = plugin.generalConfig().getConfig().getInt(cooldownPath);
        if (cooldownTime <= 0 ) return true;

        UUID uuid = player.getUniqueId();
        long lastUsage = cooldowns.getOrDefault(uuid, 0L);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUsage < cooldownTime * 1000L) {
            long remainingTime = (cooldownTime * 1000L - (currentTime - lastUsage)) / 1000L;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(format("&7You must wait &e{0} seconds&7 before teleporting again", remainingTime)));
            return false;
        }

        return true;
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
