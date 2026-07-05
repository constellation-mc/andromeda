package dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation.mixin;

import dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation.BeforeDataPackSyncEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
abstract class PlayerListMixin {

  @Shadow
  @Final
  private MinecraftServer server;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Ljava/util/Map;values()Ljava/util/Collection;",
              ordinal = 0,
              shift = At.Shift.BEFORE),
      method = "reloadResources")
  private void andromeda$reload(CallbackInfo ci) {
    // we don't sync until our advancements have been generated
    BeforeDataPackSyncEvent.EVENT.invoker().beforeDataPackReload(server);
  }
}
