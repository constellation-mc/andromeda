package dev.zenfyr.andromeda.common.mixin.alpha;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.common.client.GlobalAlphaController;
import dev.zenfyr.pulsar.util.ColorUtil;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(GuiTextRenderState.class)
public class GuiTextRenderStateMixin {

  @Final
  @Mutable
  @Shadow
  public int color;

  @Final
  @Mutable
  @Shadow
  public int backgroundColor;

  @WrapOperation(
      at =
          @At(
              value = "FIELD",
              target = "Lnet/minecraft/client/gui/render/state/GuiTextRenderState;color:I",
              opcode = Opcodes.PUTFIELD),
      method = "<init>")
  private void modifyColor(GuiTextRenderState instance, int value, Operation<Void> original) {
    var modifier = GlobalAlphaController.MODIFIER.get();
    if (modifier == null) {
      original.call(instance, value);
    } else {
      float modAlpha = modifier.apply(ColorUtil.getAlphaF(value));
      int finalColor = ColorUtil.toColor(
          ColorUtil.getRedF(value),
          ColorUtil.getGreenF(value),
          ColorUtil.getBlueF(value),
          modAlpha);
      original.call(instance, finalColor);
    }
  }

  @WrapOperation(
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/client/gui/render/state/GuiTextRenderState;backgroundColor:I",
              opcode = Opcodes.PUTFIELD),
      method = "<init>")
  private void modifyBgColor(GuiTextRenderState instance, int value, Operation<Void> original) {
    var modifier = GlobalAlphaController.MODIFIER.get();
    if (modifier == null) {
      original.call(instance, value);
    } else {
      float modAlpha = modifier.apply(ColorUtil.getAlphaF(value));
      int finalColor = ColorUtil.toColor(
          ColorUtil.getRedF(value),
          ColorUtil.getGreenF(value),
          ColorUtil.getBlueF(value),
          modAlpha);
      original.call(instance, finalColor);
    }
  }
}
