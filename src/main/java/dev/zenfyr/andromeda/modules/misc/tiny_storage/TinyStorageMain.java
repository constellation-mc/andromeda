package dev.zenfyr.andromeda.modules.misc.tiny_storage;

import dev.zenfyr.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;

public final class TinyStorageMain {
  static void init() {
    ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
      if (alive
          || Andromeda.MAIN.get(TinyStorage.CONFIG).transferMode
              == TinyStorage.TransferMode.ALWAYS_TRANSFER
          || newPlayer.level().getGameRules().get(GameRules.KEEP_INVENTORY)
          || oldPlayer.isSpectator()) {
        copyInputs(oldPlayer, newPlayer);
      }
    });
  }

  public static void copyInputs(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
    for (int i = 0; i < newPlayer.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
      newPlayer
          .inventoryMenu
          .getCraftSlots()
          .setItem(i, oldPlayer.inventoryMenu.getCraftSlots().getItem(i));
    }
  }
}
