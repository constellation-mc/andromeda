package me.melontini.andromeda.common.util;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class ServerHelper {

    @Getter @Setter
    private static MinecraftServer context;

    public static void broadcastToOps(MinecraftServer server, Text text) {
        server.getPlayerManager().broadcast(text, player -> {
            if (server.getPlayerManager().isOperator(player.getGameProfile())) {
                return text;
            }
            return null;
        }, false);
    }
}
