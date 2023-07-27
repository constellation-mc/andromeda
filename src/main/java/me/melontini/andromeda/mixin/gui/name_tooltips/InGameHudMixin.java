package me.melontini.andromeda.mixin.gui.name_tooltips;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.minecraft.client.util.DrawUtil;
import me.melontini.dark_matter.util.Utilities;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
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
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow private int heldItemTooltipFade;

    @Shadow private ItemStack currentStack;

    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Inject(at = @At("HEAD"), method = "renderHeldItemTooltip", cancellable = true)
    private void andromeda$renderTooltip(DrawContext context, CallbackInfo ci) {
        if (Andromeda.CONFIG.tooltipNotName) {
            this.client.getProfiler().push("selectedItemName");

            if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty()) {
                int l = (int)((float)this.heldItemTooltipFade * 256.0F / 10.0F);
                if (l > 255) {
                    l = 255;
                }

                if (l > 0) {
                    int k = this.scaledHeight - 59;
                    if (!this.client.interactionManager.hasStatusBars()) {
                        k += 14;
                    }

                    MatrixStack matrices = context.getMatrices();
                    matrices.push();
                    matrices.scale(1, 1, 1);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1, 1, 1, Math.min(l/255f, 0.8f));
                    var list = Screen.getTooltipFromItem(MinecraftClient.getInstance(), this.currentStack);
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

                    DrawUtil.renderTooltipFromComponents(context, list1, ((this.scaledWidth - f) / 2f) - 12, (k - j + (l/255f*2)) + 18);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    RenderSystem.disableBlend();
                    matrices.pop();
                }
            }

            this.client.getProfiler().pop();
            ci.cancel();
        }
    }
}
