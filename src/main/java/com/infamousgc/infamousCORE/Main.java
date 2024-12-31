package com.infamousgc.infamousCORE;

import com.infamousgc.infamousCORE.Data.PlayerData;
import com.infamousgc.infamousCORE.Data.PlayerDataManager;
import com.infamousgc.infamousCORE.Storage.Database;
import com.infamousgc.infamousCORE.Storage.FileManager;
import com.infamousgc.infamousCORE.Tasks.ConfirmationManager;
import com.infamousgc.infamousCORE.Tasks.CooldownManager;
import com.infamousgc.infamousCORE.Tasks.WarmupManager;
import com.infamousgc.infamousCORE.Utils.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private FileManager generalConfig;
    private FileManager conversions;
    private Database database;

    private PlayerDataManager playerDataManager;
    private ConfirmationManager confirmationManager;
    private CooldownManager cooldownManager;
    private WarmupManager warmupManager;

    private boolean disabled;

    @Override
    public void onEnable() {
        loadConfig();
        loadManagers();
        loadData();
        registrar();
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void loadConfig() {
        generalConfig = new FileManager(this, "config.yml");
        conversions = new FileManager(this, "conversions.yml");
    }

    private void loadManagers() {
        playerDataManager = new PlayerDataManager();
        confirmationManager = new ConfirmationManager(this);
        cooldownManager = new CooldownManager(this);
        warmupManager = new WarmupManager(this);
    }

    private void registrar() {
        Registrar registrar = new Registrar(this);
        registrar.registerCommands();
        registrar.registerListeners();
    }

    private void loadData() {
        this.database = new Database(this);
        database.loadData();
    }

    private void saveData() {
        Map<UUID, PlayerData> allPlayerData = playerDataManager.getAllPlayerData();
        for (UUID uuid : allPlayerData.keySet()) {
            database.setData(uuid);
        }

        Logger.info("Saving data for {0} player" + (allPlayerData.size() > 1 ? "s" : ""), allPlayerData.size());
        database.disconnect();
    }

    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public ConfirmationManager getConfirmationManager() { return confirmationManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public WarmupManager getWarmupManager() { return warmupManager; }

    public FileManager generalConfig() { return generalConfig; }
    public FileManager conversions() { return conversions; }
}
