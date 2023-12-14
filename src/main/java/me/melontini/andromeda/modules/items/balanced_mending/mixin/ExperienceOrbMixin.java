package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.items.balanced_mending.BalancedMending;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrbEntity.class)
class ExperienceOrbMixin {
    @Unique
    private static final BalancedMending am$balmend = ModuleManager.quick(BalancedMending.class);
    @Inject(at = @At("HEAD"), method = "repairPlayerGears", cancellable = true)
    private void andromeda$repair(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
        if (am$balmend.enabled()) cir.setReturnValue(amount);
    }
}
