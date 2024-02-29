package me.melontini.andromeda.modules.gui.name_tooltips.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(InGameHud.class)
abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow private int scaledHeight;

    @Inject(at = @At("HEAD"), method = "renderHeldItemTooltip", cancellable = true)
    private void andromeda$renderTooltip(DrawContext context, CallbackInfo ci) {
        this.client.getProfiler().push("selectedItemName");

        if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty() && MinecraftClient.getInstance().currentScreen == null) {
            int l = (int) ((float) this.heldItemTooltipFade * 256.0F / 10.0F);
            if (l > 255) {
                l = 255;
            }

            if (l > 0) {
                int k = this.scaledHeight - 59;
                if (!MakeSure.notNull(this.client.interactionManager).hasStatusBars()) {
                    k += 14;
                }

                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(0, 0, -450);
                matrices.scale(1, 1, 1);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1, 1, 1, Math.min(l / 255f, 0.8f));
                var list = Screen.getTooltipFromItem(MinecraftClient.getInstance(), this.currentStack);
                List<TooltipComponent> list1 = list.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());

                this.currentStack.getTooltipData().ifPresent(datax -> list1.add(1, Utilities.supply(() -> {
                    TooltipComponent component = TooltipComponentCallback.EVENT.invoker().getComponent(datax);
                    if (component == null) component = TooltipComponent.of(datax);
                    return component;
                })));

                int finalK = k;
                int finalL = l;
                context.drawTooltip(client.textRenderer, list1, 0, 0, (screenWidth, screenHeight, x, y, width, height) -> {
                    float smoothX = ((screenWidth - width) / 2f);
                    float smoothY = (finalK - height + (finalL / 255f * 2)) + 6;
                    matrices.translate(smoothX - (int) smoothX, smoothY - (int) smoothY, 1);
                    return new Vector2i((int) smoothX, (int) smoothY);
                });
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
                matrices.pop();
            }
        }

        this.client.getProfiler().pop();
        ci.cancel();
    }
}
