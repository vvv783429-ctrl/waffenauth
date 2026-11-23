package ru.waffenbot.auth;

import org.bukkit.entity.Player;

public class GeyserUtil {

    private static Boolean floodgateAvailable = null;

    public static boolean isBedrockPlayer(Player player) {
        if (floodgateAvailable == null) {
            try {
                Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                floodgateAvailable = true;
            } catch (ClassNotFoundException e) {
                floodgateAvailable = false;
            }
        }
        if (!floodgateAvailable) return false;

        try {
            return org.geysermc.floodgate.api.FloodgateApi.getInstance()
                    .isFloodgatePlayer(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }
}
