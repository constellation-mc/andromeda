package me.melontini.andromeda.mixin.mechanics.dragon_fight;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.mechanics.dragon_fight.DragonFight;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragonEntity.class)
@Feature({"dragonFight.fightTweaks", "dragonFight.shorterCrystalTrackRange"})
class EnderDragonMixin {
    @Unique
    private static final DragonFight am$dft = ModuleManager.quick(DragonFight.class);
    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "doubleValue=32"), method = "tickWithEndCrystals")
    private double andromeda$modConstant(double constant) {
        if (am$dft.config().enabled && am$dft.config().shorterCrystalTrackRange) return 24.0;
        return constant;
    }
}
