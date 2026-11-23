package ru.waffenbot.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WaffenAuthCommand implements CommandExecutor {

    private final MessageService msg;

    public WaffenAuthCommand(MessageService msg) {
        this.msg = msg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            for (String line : msg.getList("help")) {
                sender.sendMessage(line);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("waffenauth.admin")) {
                sender.sendMessage(MessageService.color("&cУ вас нет прав."));
                return true;
            }
            msg.reload();
            sender.sendMessage(MessageService.color("&aКонфиг перезагружен."));
            return true;
        }
        sender.sendMessage(MessageService.color("&cИспользуйте: /waffenauth help"));
        return true;
    }
}
