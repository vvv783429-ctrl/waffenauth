package ru.waffenbot.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AuthPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private MessageService messageService;

    @Override
    public void onEnable() {
        messageService = new MessageService(this);
        databaseManager = new DatabaseManager(this);
        authManager = new AuthManager();

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addFilter(new Log4jFilter());

        AuthCommand command = new AuthCommand(this, databaseManager, authManager, messageService);
        getCommand("register").setExecutor(command);
        getCommand("login").setExecutor(command);
        getCommand("logout").setExecutor(command);
        getCommand("changepass").setExecutor(command);
        getCommand("waffenauth").setExecutor(new WaffenAuthCommand(messageService));

        getServer().getPluginManager().registerEvents(
                new AuthListener(this, authManager, databaseManager, messageService), this);

        startReminderTask();
        getLogger().info("WaffenAuth включён. База: auth.db");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void startReminderTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (authManager.isLoggedIn(player.getUniqueId())) continue;
                    if (databaseManager.isRegistered(player.getUniqueId())) {
                        messageService.send(player, "reminder-login");
                    } else {
                        messageService.send(player, "reminder-register");
                    }
                }
            }
        }.runTaskTimer(this, 0L, 40L);
    }
}
