package dev.zenfyr.andromeda.common.mixin.alpha;

import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.common.client.GlobalAlphaController;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import dev.zenfyr.pulsar.api.util.ColorUtil;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(BlitRenderState.class)
public class BlitRenderStateMixin {

  @ModifyVariable(
      at = @At(value = "LOAD"),
      index = 12,
      method =
          "<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2fc;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V",
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
