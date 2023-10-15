package me.melontini.andromeda.mixin.gui.name_tooltips;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(InGameHud.class)
@MixinRelatedConfigOption("tooltipNotName")
abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private int heldItemTooltipFade;

    @Shadow
    private ItemStack currentStack;

    @Shadow
    private int scaledHeight;

    @Shadow
    private int scaledWidth;

    @Inject(at = @At("HEAD"), method = "renderHeldItemTooltip", cancellable = true)
    private void andromeda$renderTooltip(MatrixStack matrices, CallbackInfo ci) {
        if (!Config.get().tooltipNotName) return;

        this.client.getProfiler().push("selectedItemName");

        if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty() && MinecraftClient.getInstance().currentScreen == null) {
            int l = (int) ((float) this.heldItemTooltipFade * 256.0F / 10.0F);
            if (l > 255) {
                l = 255;
            }

            if (l > 0) {
                int k = this.scaledHeight - 59;
                if (!this.client.interactionManager.hasStatusBars()) {
                    k += 14;
                }

                matrices.push();
                matrices.translate(0, 0, -450);
                matrices.scale(1, 1, 1);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1, 1, 1, Math.min(l / 255f, 0.8f));
                var list = DrawUtil.FAKE_SCREEN.getTooltipFromItem(this.currentStack);
                List<TooltipComponent> list1 = list.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());

                this.currentStack.getTooltipData().ifPresent(datax -> list1.add(1, Utilities.supply(() -> {
                    TooltipComponent component = TooltipComponentCallback.EVENT.invoker().getComponent(datax);
                    if (component == null) component = TooltipComponent.of(datax);
                    return component;
                })));

                int j = 0;
                int f = 0;
                for (TooltipComponent tooltipComponent : list1) {
                    j += tooltipComponent.getHeight();
                    int t = tooltipComponent.getWidth(this.client.textRenderer);
                    if (t > f) f = t;
                }

                DrawUtil.renderTooltipFromComponents(matrices, list1, ((this.scaledWidth - f) / 2f) - 12, (k - j + (l / 255f * 2)) + 18);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
                matrices.pop();
            }
        }

        this.client.getProfiler().pop();
        ci.cancel();
    }
}
