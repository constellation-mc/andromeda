package me.melontini.andromeda.modules.gui.name_tooltips.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class InGameHudMixin {
  @Shadow
  @Final
  private Minecraft minecraft;

  @Shadow
  private int toolHighlightTimer;

  @Shadow
  private ItemStack lastToolHighlight;

  @Shadow
  private int screenHeight;

  @Inject(at = @At("HEAD"), method = "renderSelectedItemName", cancellable = true)
  private void andromeda$renderTooltip(GuiGraphics context, CallbackInfo ci) {
    this.minecraft.getProfiler().push("selectedItemName");

    if (this.toolHighlightTimer > 0
        && !this.lastToolHighlight.isEmpty()
        && Minecraft.getInstance().screen == null) {
      int l = (int) ((float) this.toolHighlightTimer * 256.0F / 10.0F);
      if (l > 255) {
        l = 255;
      }

      if (l > 0) {
        int k = this.screenHeight - 59;
        if (!MakeSure.notNull(this.minecraft.gameMode).canHurtPlayer()) {
          k += 14;
        }

        PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.translate(0, 0, -450);
        matrices.scale(1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, Math.min(l / 255f, 0.8f));
        var list = Screen.getTooltipFromItem(Minecraft.getInstance(), this.lastToolHighlight);
        List<ClientTooltipComponent> list1 = list.stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toCollection(ArrayList::new));

        this.lastToolHighlight
            .getTooltipImage()
            .ifPresent(datax -> list1.add(1, Utilities.supply(() -> {
              ClientTooltipComponent component =
                  TooltipComponentCallback.EVENT.invoker().getComponent(datax);
              if (component == null) component = ClientTooltipComponent.create(datax);
              return component;
            })));

        int finalK = k;
        int finalL = l;
        context.renderTooltipInternal(
            minecraft.font, list1, 0, 0, (screenWidth, screenHeight, x, y, width, height) -> {
              float smoothX = ((screenWidth - width) / 2f);
              float smoothY = (finalK - height + (finalL / 255f * 2)) + 6;
              matrices.translate(smoothX - (int) smoothX, smoothY - (int) smoothY, 1);
              return new Vector2i((int) smoothX, (int) smoothY);
            });
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        matrices.popPose();
      }
    }

    this.minecraft.getProfiler().pop();
    ci.cancel();
  }
}
