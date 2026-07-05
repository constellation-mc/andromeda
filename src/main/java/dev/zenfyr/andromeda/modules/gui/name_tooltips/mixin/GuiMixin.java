package dev.zenfyr.andromeda.modules.gui.name_tooltips.mixin;

import dev.zenfyr.andromeda.common.client.GlobalAlphaController;
import dev.zenfyr.pulsar.api.util.MakeSure;
import dev.zenfyr.pulsar.api.util.Utilities;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class GuiMixin {
  @Shadow
  @Final
  private Minecraft minecraft;

  @Shadow
  private int toolHighlightTimer;

  @Shadow
  private ItemStack lastToolHighlight;

  @Inject(at = @At("HEAD"), method = "extractSelectedItemName", cancellable = true)
  private void andromeda$renderTooltip(GuiGraphicsExtractor graphics, CallbackInfo ci) {
    Profiler.get().push("selectedItemName");

    if (this.toolHighlightTimer > 0
        && !this.lastToolHighlight.isEmpty()
        && Minecraft.getInstance().screen == null) {
      int l = (int) ((float) this.toolHighlightTimer * 256.0F / 10.0F);
      if (l > 255) {
        l = 255;
      }

      if (l > 0) {
        int k = graphics.guiHeight() - 59;
        if (!MakeSure.notNull(this.minecraft.gameMode).canHurtPlayer()) {
          k += 14;
        }

        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.scale(1, 1);

        try {
          int finalK = k;
          int finalL = l;

          GlobalAlphaController.MODIFIER.set(key -> {
            float flowAlpha = Math.min(Math.min(finalL / 255f, 0.8f), 0.8f);
            return key * flowAlpha;
          });

          var list = Screen.getTooltipFromItem(Minecraft.getInstance(), this.lastToolHighlight);
          List<ClientTooltipComponent> list1 = list.stream()
              .map(Component::getVisualOrderText)
              .map(ClientTooltipComponent::create)
              .collect(Collectors.toCollection(ArrayList::new));

          this.lastToolHighlight
              .getTooltipImage()
              .ifPresent(datax -> list1.add(1, Utilities.supply(() -> {
                ClientTooltipComponent component =
                    ClientTooltipComponentCallback.EVENT.invoker().getClientComponent(datax);
                if (component == null) component = ClientTooltipComponent.create(datax);
                return component;
              })));

          graphics.tooltip(
              minecraft.font,
              list1,
              0,
              0,
              (screenWidth, screenHeight, x, y, width, height) -> {
                float smoothX = ((screenWidth - width) / 2f);
                float smoothY = (finalK - height + (finalL / 255f * 2)) + 6;
                matrices.translate(smoothX - (int) smoothX, smoothY - (int) smoothY);
                return new Vector2i((int) smoothX, (int) smoothY);
              },
              null);
        } finally {
          GlobalAlphaController.MODIFIER.remove();
        }
        matrices.popMatrix();
      }
    }

    Profiler.get().pop();
    ci.cancel();
  }
}
