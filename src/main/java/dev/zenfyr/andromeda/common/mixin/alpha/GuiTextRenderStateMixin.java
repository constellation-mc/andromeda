package dev.zenfyr.andromeda.common.mixin.alpha;

import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.common.client.GlobalAlphaController;
import dev.zenfyr.pulsar.api.util.ColorUtil;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(GuiTextRenderState.class)
public class GuiTextRenderStateMixin {

  @ModifyVariable(at = @At(value = "LOAD"), index = 6, method = "<init>", argsOnly = true)
  private int modifyColor(int color) {
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

  @ModifyVariable(at = @At(value = "LOAD"), index = 7, method = "<init>", argsOnly = true)
  private int modifyBgColor(int color) {
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
