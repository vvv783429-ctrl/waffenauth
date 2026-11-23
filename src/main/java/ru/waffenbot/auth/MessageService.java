package ru.waffenbot.auth;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageService {

    private final AuthPlugin plugin;
    private FileConfiguration config;

    public MessageService(AuthPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    private String prefix() {
        return color(config.getString("messages.prefix", ""));
    }

    public String raw(String key) {
        return config.getString("messages." + key, key);
    }

    public String get(String key) {
        return prefix() + color(raw(key));
    }

    public String get(String key, String... placeholders) {
        String msg = raw(key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return prefix() + color(msg);
    }

    public List<String> getList(String key) {
        return config.getStringList("messages." + key).stream()
                .map(MessageService::color)
                .toList();
    }

    public int minPasswordLength() {
        return config.getInt("settings.min-password-length", 6);
    }

    public int kickTimeoutSeconds() {
        return config.getInt("settings.kick-timeout-seconds", 60);
    }

    public void send(Player player, String key, String... placeholders) {
        player.sendMessage(get(key, placeholders));
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
