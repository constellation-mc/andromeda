package me.melontini.andromeda.modules.misc.tiny_storage;

import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

public final class Main {
    static void init() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (alive || Andromeda.ROOT_HANDLER.get(TinyStorage.CONFIG).transferMode == TinyStorage.TransferMode.ALWAYS_TRANSFER
                    || newPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)
                    || oldPlayer.isSpectator()) {
                copyInputs(oldPlayer, newPlayer);
            }
        });
    }

    public static void copyInputs(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        for (int i = 0; i < newPlayer.playerScreenHandler.getCraftingInput().size(); i++) {
            newPlayer.playerScreenHandler.getCraftingInput().setStack(i, oldPlayer.playerScreenHandler.getCraftingInput().getStack(i));
        }
    }
}
