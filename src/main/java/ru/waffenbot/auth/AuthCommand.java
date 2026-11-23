package ru.waffenbot.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

public class AuthCommand implements CommandExecutor {

    private final AuthPlugin plugin;
    private final DatabaseManager data;
    private final AuthManager auth;
    private final MessageService msg;

    public AuthCommand(AuthPlugin plugin, DatabaseManager data, AuthManager auth, MessageService msg) {
        this.plugin = plugin;
        this.data = data;
        this.auth = auth;
        this.msg = msg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков.");
            return true;
        }

        switch (cmd.getName().toLowerCase()) {
            case "register" -> handleRegister(player, args);
            case "login" -> handleLogin(player, args);
            case "logout" -> handleLogout(player);
            case "changepass" -> handleChangePass(player, args);
            default -> { return false; }
        }
        return true;
    }

    private void handleRegister(Player player, String[] args) {
        if (auth.isLoggedIn(player.getUniqueId())) { msg.send(player, "already-logged-in"); return; }
        if (data.isRegistered(player.getUniqueId())) { msg.send(player, "register-already"); return; }
        if (args.length != 2) { msg.send(player, "register-usage"); return; }
        if (!args[0].equals(args[1])) { msg.send(player, "register-passwords-differ"); return; }

        int min = msg.minPasswordLength();
        if (args[0].length() < min) {
            msg.send(player, "register-password-short", "%min%", String.valueOf(min));
            return;
        }

        String hash = BCrypt.hashpw(args[0], BCrypt.gensalt());
        data.savePassword(player.getUniqueId(), hash);
        savePlayerIp(player);
        auth.setLoggedIn(player.getUniqueId());
        msg.send(player, "register-success");
    }

    private void handleLogin(Player player, String[] args) {
        if (auth.isLoggedIn(player.getUniqueId())) { msg.send(player, "already-logged-in"); return; }
        if (!data.isRegistered(player.getUniqueId())) { msg.send(player, "login-not-registered"); return; }
        if (args.length != 1) { msg.send(player, "login-usage"); return; }

        int max = plugin.getConfig().getInt("settings.max-login-attempts", 5);
        String hash = data.getHash(player.getUniqueId());
        if (hash == null || !BCrypt.checkpw(args[0], hash)) {
            int failed = auth.incrementFailed(player.getUniqueId());
            if (failed >= max) {
                player.kickPlayer(msg.get("login-attempts-exceeded"));
                auth.cancelKickTask(player.getUniqueId());
                return;
            }
            int left = max - failed;
            msg.send(player, "login-fail", "%left%", String.valueOf(left));
            return;
        }

        savePlayerIp(player);
        auth.setLoggedIn(player.getUniqueId());
        msg.send(player, "login-success");
    }

    private void handleLogout(Player player) {
        if (!auth.isLoggedIn(player.getUniqueId())) { msg.send(player, "not-logged-in"); return; }
        auth.logout(player.getUniqueId());
        msg.send(player, "logout-success");
    }

    private void handleChangePass(Player player, String[] args) {
        if (!auth.isLoggedIn(player.getUniqueId())) { msg.send(player, "not-logged-in"); return; }
        if (args.length != 2) { msg.send(player, "changepass-usage"); return; }

        String hash = data.getHash(player.getUniqueId());
        if (hash == null || !BCrypt.checkpw(args[0], hash)) {
            msg.send(player, "changepass-wrong-old");
            return;
        }
        int min = msg.minPasswordLength();
        if (args[1].length() < min) {
            msg.send(player, "changepass-short", "%min%", String.valueOf(min));
            return;
        }

        String newHash = BCrypt.hashpw(args[1], BCrypt.gensalt());
        data.savePassword(player.getUniqueId(), newHash);
        msg.send(player, "changepass-success");
    }

    private void savePlayerIp(Player player) {
        if (player.getAddress() != null && player.getAddress().getAddress() != null) {
            data.saveIp(player.getUniqueId(), player.getAddress().getAddress().getHostAddress());
        }
    }
}
