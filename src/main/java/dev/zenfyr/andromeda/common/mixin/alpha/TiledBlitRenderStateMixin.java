package dev.zenfyr.andromeda.common.mixin.alpha;

import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.common.client.GlobalAlphaController;
import dev.zenfyr.pulsar.api.util.ColorUtil;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.render.state.TiledBlitRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(TiledBlitRenderState.class)
public class TiledBlitRenderStateMixin {

  @ModifyVariable(
      at = @At(value = "LOAD"),
      index = 14,
      method =
          "<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V",
      argsOnly = true)
  private static int modifyColor(int color) {
    var modifier = GlobalAlphaController.MODIFIER.get();
    if (modifier == null) {
      return color;
    } else {
      float modAlpha = modifier.apply(ColorUtil.getAlphaF(color));
      return ColorUtil.toColor(
          ColorUtil.getRedF(color),
          ColorUtil.getGreenF(color),
          ColorUtil.getBlueF(color),
          modAlpha);
    }
  }
}
