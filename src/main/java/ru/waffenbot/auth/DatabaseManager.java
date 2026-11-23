package ru.waffenbot.auth;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        connect();
        createTable();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(plugin.getDataFolder(), "auth.db");
            plugin.getDataFolder().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось подключиться к SQLite: " + e.getMessage());
        }
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players (
                uuid TEXT PRIMARY KEY,
                hash TEXT NOT NULL,
                last_ip TEXT
            );
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось создать таблицу: " + e.getMessage());
        }
    }

    public boolean isRegistered(UUID uuid) {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            return ps.executeQuery().next();
        } catch (SQLException e) {
            plugin.getLogger().severe("isRegistered error: " + e.getMessage());
            return false;
        }
    }

    public String getHash(UUID uuid) {
        String sql = "SELECT hash FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("hash");
        } catch (SQLException e) {
            plugin.getLogger().severe("getHash error: " + e.getMessage());
        }
        return null;
    }

    public String getLastIp(UUID uuid) {
        String sql = "SELECT last_ip FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("last_ip");
        } catch (SQLException e) {
            plugin.getLogger().severe("getLastIp error: " + e.getMessage());
        }
        return null;
    }

    public void savePassword(UUID uuid, String hash) {
        String sql = "INSERT INTO players (uuid, hash) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET hash = excluded.hash";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, hash);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("savePassword error: " + e.getMessage());
        }
    }

    public void saveIp(UUID uuid, String ip) {
        String sql = "UPDATE players SET last_ip = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("saveIp error: " + e.getMessage());
        }
    }

    public void removePassword(UUID uuid) {
        String sql = "DELETE FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("removePassword error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("close error: " + e.getMessage());
        }
    }
}
