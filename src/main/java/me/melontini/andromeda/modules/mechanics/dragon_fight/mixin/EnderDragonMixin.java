package me.melontini.andromeda.modules.mechanics.dragon_fight.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.mechanics.dragon_fight.DragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragonEntity.class)
class EnderDragonMixin {

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "doubleValue=32"), method = "tickWithEndCrystals")
    private double andromeda$modConstant(double constant) {
        if (ModuleManager.quick(DragonFight.class).config().shorterCrystalTrackRange) return 24.0;
        return constant;
    }
}
