package com.infamousgc.infamousCORE.Storage;

import com.infamousgc.infamousCORE.Main;
import com.infamousgc.infamousCORE.ModuleManager;
import com.infamousgc.infamousCORE.Utils.Logger;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Manages MySQL database connections and operations
 */
public class MySQL {
    private static final String CREATE_HOMES_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS core_homes (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "owner CHAR(36) NOT NULL, " +
            "home_name VARCHAR(50) NOT NULL, " +
            "world VARCHAR(50) NOT NULL, " +
            "x DOUBLE NOT NULL, " +
            "y DOUBLE NOT NULL, " +
            "z DOUBLE NOT NULL, " +
            "yaw FLOAT NOT NULL, " +
            "pitch FLOAT NOT NULL, " +
            "UNIQUE KEY uk_owner_home (owner, home_name)" +
            ")";

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private Connection connection;

    /**
     * Constructs a new MySQL instance
     *
     * @param plugin The main plugin instance
     */
    public MySQL(Main plugin) {
        FileConfiguration config = plugin.generalConfig().getConfig();
        this.jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&autoReconnect=true",
                config.getString("mysql.host"),
                config.getString("mysql.port"),
                config.getString("mysql.database"));
        this.username = config.getString("mysql.username");
        this.password = config.getString("mysql.password");

        if (!validLogin()) {
            Logger.severe("MySQL connection defined in config.yml is invalid. Some modules require a MySQL connection " +
                    "to function.");
            ModuleManager.HOME.disable();
            return;
        }

        this.connection = getConnection();
        createHomeTable();
    }

    private boolean validLogin() {
        return jdbcUrl != null && username != null;
    }

    /**
     * Returns the current connection, opens a new one if none is found
     *
     * @return The Connection object
     */
    protected Connection getConnection() {
        if (isConnected()) return connection;

        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(jdbcUrl, username, password);
                Logger.info("Connected to MySQL database");
            }
        } catch (SQLException e) {
            Logger.severe("Failed to close MySQL connection: {0}", e.getMessage());
            Arrays.stream(e.getStackTrace())
                    .forEach(element -> Logger.log(Level.SEVERE, element.toString()));
        }

        return connection;
    }

    /**
     * Closes the MySQL connection
     */
    public void disconnect() {
        if (!isConnected()) return;

        try {
            connection.close();
            Logger.info("MySQL connection closed successfully");
        } catch (SQLException e) {
            Logger.severe("Failed to close MySQL connection: {0}", e.getMessage());
            Arrays.stream(e.getStackTrace())
                    .forEach(element -> Logger.log(Level.SEVERE, element.toString()));
        }
    }

    /**
     * Checks if the MySQL connection is established
     *
     * @return True if connected, false otherwise
     */
    private boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Creates a table to store data for player homes
     */
    private void createHomeTable() {
        try (PreparedStatement statement = connection.prepareStatement(CREATE_HOMES_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.severe("Failed to create homes table: " + e.getMessage());
            Logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
        }
    }
}
