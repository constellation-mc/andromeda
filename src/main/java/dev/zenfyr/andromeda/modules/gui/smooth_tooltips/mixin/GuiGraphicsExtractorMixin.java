package dev.zenfyr.andromeda.modules.gui.smooth_tooltips.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.gui.smooth_tooltips.SmoothTooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
abstract class GuiGraphicsExtractorMixin {

  @Shadow
  @Final
  private Minecraft minecraft;

  @Shadow
  @Final
  private Matrix3x2fStack pose;

  @Unique private static Vector2d smoothPos;

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"),
      method = {
        "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
        "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V"
      })
  private Vector2ic andromeda$smoothTooltip(
      Vector2ic vic,
      @Local(argsOnly = true, name = "xo") int xo,
      @Local(argsOnly = true, name = "yo") int yo,
      @Share("popMatrix") LocalBooleanRef popMatrix) {
    if (andromeda$makeSmooth(xo, yo)) {
      var c = AndromedaClient.CLIENT.get(SmoothTooltips.CONFIG);
      if (smoothPos == null) smoothPos = new Vector2d(xo, yo);
      smoothPos.x = Mth.clamp(
          Mth.lerp(
              c.deltaX * minecraft.getDeltaTracker().getGameTimeDeltaTicks(), smoothPos.x, vic.x()),
          vic.x() - c.clampX,
          vic.x() + c.clampX);
      smoothPos.y = Mth.clamp(
          Mth.lerp(
              c.deltaY * minecraft.getDeltaTracker().getGameTimeDeltaTicks(), smoothPos.y, vic.y()),
          vic.y() - c.clampY,
          vic.y() + c.clampY);

      popMatrix.set(true);
      this.pose.pushMatrix();
      this.pose.translate(
          (float) (smoothPos.x - (int) smoothPos.x), (float) (smoothPos.y - (int) smoothPos.y));
      return new Vector2i((int) smoothPos.x, (int) smoothPos.y);
    }
    return vic;
  }

  @Unique private boolean andromeda$makeSmooth(int x, int y) {
    double mX = (this.minecraft.mouseHandler.xpos()
        * this.minecraft.getWindow().getGuiScaledWidth()
        / this.minecraft.getWindow().getScreenWidth());
    if ((int) mX != x) return false;
    double mY = (this.minecraft.mouseHandler.ypos()
        * this.minecraft.getWindow().getGuiScaledHeight()
        / this.minecraft.getWindow().getScreenHeight());
    return (int) mY == y;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;",
              shift = At.Shift.AFTER),
      method = {
        "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
        "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V"
      })
  private void andromeda$popMatrix(CallbackInfo ci, @Share("popMatrix") LocalBooleanRef popMatrix) {
    if (popMatrix.get()) this.pose.popMatrix();
  }
}
