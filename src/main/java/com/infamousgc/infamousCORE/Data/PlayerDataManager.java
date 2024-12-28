package com.infamousgc.infamousCORE.Data;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public PlayerData getPlayerData(UUID uuid) {
        return players.computeIfAbsent(uuid, PlayerData::new);
    }

    public void removePlayerData(Player player) {
        players.remove(player.getUniqueId());
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return new HashMap<>(players);
    }

    // This method will be called when the plugin is disabled
    public void saveAllData() {
        // TODO: Implement saving all player data to MySQL
    }

    // This method will be called when the plugin is enabled
    public void loadAllData() {
        // TODO: Implement loading all player data from MySQL
    }
}
