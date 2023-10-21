package me.melontini.andromeda.mixin.mechanics.dragon_fight;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragonEntity.class)
@Feature({"dragonFight.fightTweaks", "dragonFight.shorterCrystalTrackRange"})
class EnderDragonMixin {
    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "doubleValue=32"), method = "tickWithEndCrystals")
    private double andromeda$modConstant(double constant) {
        if (Config.get().dragonFight.fightTweaks && Config.get().dragonFight.shorterCrystalTrackRange) return 24.0;
        return constant;
    }
}
