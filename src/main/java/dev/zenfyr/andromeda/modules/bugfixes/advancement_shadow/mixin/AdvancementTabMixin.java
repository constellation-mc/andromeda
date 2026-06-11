package dev.zenfyr.andromeda.modules.bugfixes.advancement_shadow.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AdvancementTab.class)
abstract class AdvancementTabMixin {

  @Shadow
  private float fade;

  @Shadow
  @Final
  private Minecraft minecraft;

  @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 0),
      index = 0,
      method = "extractTooltips")
  private float andromeda$draw(float value) {
    return this.fade + (0.04F * minecraft.getDeltaTracker().getGameTimeDeltaTicks());
  }

  @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 1),
      index = 0,
      method = "extractTooltips")
  private float andromeda$draw1(float value) {
    return this.fade - (0.06F * minecraft.getDeltaTracker().getGameTimeDeltaTicks());
  }
}
