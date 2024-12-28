package com.infamousgc.infamousCORE.Data;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;

    private final Map<String, Location> homes = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUserID() {
        return uuid;
    }

    public void setHome(String name, Location loc) {
        homes.put(name.toLowerCase(), loc);
    }

    public boolean hasHome(String name) {
        return homes.containsKey(name.toLowerCase());
    }

    public void deleteHome(String name) {
        homes.remove(name.toLowerCase());
    }

    public Map<String, Location> getHomes() {
        return new HashMap<>(homes);
    }

    public int getHomeCount() {
        return homes.size();
    }
}
