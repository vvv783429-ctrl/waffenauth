package ru.waffenbot.auth;

import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuthManager {

    private final Set<UUID> loggedIn = new HashSet<>();
    private final Map<UUID, BukkitTask> kickTasks = new HashMap<>();
    private final Map<UUID, Integer> failedAttempts = new HashMap<>();

    public boolean isLoggedIn(UUID uuid) { return loggedIn.contains(uuid); }

    public void setLoggedIn(UUID uuid) {
        loggedIn.add(uuid);
        failedAttempts.remove(uuid);
        cancelKickTask(uuid);
    }

    public void logout(UUID uuid) {
        loggedIn.remove(uuid);
        cancelKickTask(uuid);
    }

    public void setKickTask(UUID uuid, BukkitTask task) {
        cancelKickTask(uuid);
        kickTasks.put(uuid, task);
    }

    public void cancelKickTask(UUID uuid) {
        BukkitTask task = kickTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    public int incrementFailed(UUID uuid) { return failedAttempts.merge(uuid, 1, Integer::sum); }
    public void resetFailed(UUID uuid) { failedAttempts.remove(uuid); }
}
