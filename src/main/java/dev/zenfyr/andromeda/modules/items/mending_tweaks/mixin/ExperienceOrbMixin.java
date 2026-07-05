package dev.zenfyr.andromeda.modules.items.mending_tweaks.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
abstract class ExperienceOrbMixin {

  @Inject(at = @At("HEAD"), method = "repairPlayerItems", cancellable = true)
  private void andromeda$repair(
      ServerPlayer player, int amount, CallbackInfoReturnable<Integer> cir) {
    cir.setReturnValue(amount);
  }
}
