package me.melontini.andromeda.common.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class ServerHelper {

    public static void broadcastToOps(MinecraftServer server, Text text) {
        server.getPlayerManager().broadcast(text, player -> {
            if (server.getPlayerManager().isOperator(player.getGameProfile())) {
                return text;
            }
            return null;
        }, false);
    }
}
