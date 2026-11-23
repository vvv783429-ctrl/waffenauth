package ru.waffenbot.auth;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class AuthListener implements Listener {

    private final AuthPlugin plugin;
    private final AuthManager auth;
    private final DatabaseManager data;
    private final MessageService msg;

    private static final Set<String> ALLOWED_COMMANDS = Set.of("/register", "/login", "/waffenauth", "/wa");

    public AuthListener(AuthPlugin plugin, AuthManager auth, DatabaseManager data, MessageService msg) {
        this.plugin = plugin;
        this.auth = auth;
        this.data = data;
        this.msg = msg;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        auth.logout(player.getUniqueId());

        boolean autoEnabled = plugin.getConfig().getBoolean("settings.auto-login-by-ip", false);
        String lastIp = data.getLastIp(player.getUniqueId());
        String currentIp = (player.getAddress() != null && player.getAddress().getAddress() != null)
                ? player.getAddress().getAddress().getHostAddress() : null;

        if (autoEnabled && data.isRegistered(player.getUniqueId())
                && lastIp != null && lastIp.equals(currentIp)) {
            auth.setLoggedIn(player.getUniqueId());
            msg.send(player, "auto-login-success");
            return;
        }

        if (data.isRegistered(player.getUniqueId())) {
            msg.send(player, "reminder-login");
        } else {
            msg.send(player, "reminder-register");
        }

        int seconds = msg.kickTimeoutSeconds();
        var task = new BukkitRunnable() {
            @Override
            public void run() {
                Player p = plugin.getServer().getPlayer(player.getUniqueId());
                if (p != null && p.isOnline() && !auth.isLoggedIn(player.getUniqueId())) {
                    p.kickPlayer(msg.get("kick-timeout"));
                }
            }
        }.runTaskLater(plugin, seconds * 20L);

        auth.setKickTask(player.getUniqueId(), task);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        auth.logout(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (auth.isLoggedIn(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        msg.send(event.getPlayer(), "not-logged-in");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (auth.isLoggedIn(event.getPlayer().getUniqueId())) return;
        String cmd = event.getMessage().split(" ")[0].toLowerCase();
        if (ALLOWED_COMMANDS.contains(cmd)) return;
        event.setCancelled(true);
        msg.send(event.getPlayer(), "not-logged-in");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (auth.isLoggedIn(event.getPlayer().getUniqueId())) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setTo(from);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (auth.isLoggedIn(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (auth.isLoggedIn(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (auth.isLoggedIn(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
    }
}
