package me.melontini.andromeda.modules.mechanics.dragon_fight.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.mechanics.dragon_fight.DragonFight;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragon.class)
abstract class EnderDragonMixin {

  @ModifyExpressionValue(
      at = @At(value = "CONSTANT", args = "doubleValue=32"),
      method = "checkCrystals")
  private double andromeda$modConstant(double constant) {
    if (Andromeda.MAIN.get(DragonFight.CONFIG).shorterCrystalTrackRange) return 24.0;
    return constant;
  }
}
