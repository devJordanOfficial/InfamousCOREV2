package com.infamousgc.infamousCORE.Storage;

import com.infamousgc.infamousCORE.Data.PlayerData;
import com.infamousgc.infamousCORE.Data.PlayerDataManager;
import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.Utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages database operations for player homes
 */
public class Database extends MySQL {
    private static final String INSERT_HOME_SQL =
            "INSERT INTO core_homes (owner, home_name, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?,?)";
    private static final String SELECT_ALL_HOMES_SQL = "SELECT * FROM core_homes";
    private static final String DELETE_PLAYER_HOMES_SQL = "DELETE FROM core_homes WHERE owner = ?";

    private final Main plugin;

    /**
     * Constructs a new Database instance
     *
     * @param plugin
     */
    public Database(Main plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Saves player home data to the database
     *
     * @param uuid The UUID of the player
     */
    public void setData(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);

        try (Connection connection = getConnection()) {
            // First, delete all existing homes for the player
            try (PreparedStatement statement = connection.prepareStatement(DELETE_PLAYER_HOMES_SQL)) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            }

            // Then, insert all current homes
            try (PreparedStatement statement = connection.prepareStatement(INSERT_HOME_SQL)) {
                for (Map.Entry<String, Location> entry : data.getHomes().entrySet()) {
                    String homeName = entry.getKey();
                    Location loc = entry.getValue();
                    String world = loc.getWorld() != null ? loc.getWorld().getName() : "null";

                    statement.setString(1, uuid.toString());
                    statement.setString(2, homeName);
                    statement.setString(3, world);
                    statement.setDouble(4, loc.getX());
                    statement.setDouble(5, loc.getY());
                    statement.setDouble(6, loc.getZ());
                    statement.setFloat(7, loc.getYaw());
                    statement.setFloat(8, loc.getPitch());

                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            Logger.severe("Failed to save data to MySQL: {0}", e.getMessage());
            Logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Loads all player home data from the database
     */
    public void loadData() {

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_HOMES_SQL);
             ResultSet result = statement.executeQuery()) {

            while(result.next()) {
                UUID owner = UUID.fromString(result.getString("owner"));
                String homeName = result.getString("home_name");
                String world = result.getString("world");
                double x = result.getDouble("x");
                double y = result.getDouble("y");
                double z = result.getDouble("z");
                float yaw = result.getFloat("yaw");
                float pitch = result.getFloat("pitch");

                Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

                PlayerData data = plugin.getPlayerDataManager().getPlayerData(owner);
                data.setHome(homeName, loc);
            }
        } catch (SQLException e) {
            Logger.severe("Failed to load data from MySQL: {0}", e.getMessage());
            Logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
        }
    }
}
