package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrbEntity.class)
abstract class ExperienceOrbMixin {

    @Inject(at = @At("HEAD"), method = "repairPlayerGears", cancellable = true)
    private void andromeda$repair(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(amount);
    }
}
