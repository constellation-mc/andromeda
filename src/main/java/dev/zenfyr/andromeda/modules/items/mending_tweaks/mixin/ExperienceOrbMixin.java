package dev.zenfyr.andromeda.modules.items.mending_tweaks.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
abstract class ExperienceOrbMixin {

  @Inject(at = @At("HEAD"), method = "repairPlayerItems", cancellable = true)
  private void andromeda$repair(Player player, int amount, CallbackInfoReturnable<Integer> cir) {
    cir.setReturnValue(amount);
  }
}
