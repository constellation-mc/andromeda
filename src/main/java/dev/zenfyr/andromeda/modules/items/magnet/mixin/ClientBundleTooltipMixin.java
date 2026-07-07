package dev.zenfyr.andromeda.modules.items.magnet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.modules.items.magnet.client.ClientMagnetTooltip;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(ClientBundleTooltip.class)
abstract class ClientBundleTooltipMixin {

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;extractProgressbar(IILnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lorg/apache/commons/lang3/math/Fraction;)V"),
      method = "extractBundleWithItemsTooltip")
  private void andromeda$skipProgress(
      int x,
      int y,
      Font font,
      GuiGraphicsExtractor graphics,
      Fraction weight,
      Operation<Void> original) {
    if (ClientMagnetTooltip.IS_MAGNET.orElse(Boolean.FALSE)) return;
    original.call(x, y, font, graphics, weight);
  }

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;extractProgressbar(IILnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lorg/apache/commons/lang3/math/Fraction;)V"),
      method = "extractEmptyBundleTooltip")
  private static void andromeda$skipEmptyProgress(
      int x,
      int y,
      Font font,
      GuiGraphicsExtractor graphics,
      Fraction weight,
      Operation<Void> original) {
    if (ClientMagnetTooltip.IS_MAGNET.orElse(Boolean.FALSE)) return;
    original.call(x, y, font, graphics, weight);
  }
}
