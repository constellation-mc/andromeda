package me.melontini.andromeda.modules.gui.item_frame_tooltips.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.stream.Collectors;

public class Client {

    private static ItemStack frameStack = ItemStack.EMPTY;
    private static float tooltipFlow;
    private static float oldTooltipFlow;

    public static void init() {
        inGameTooltips();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            var cast = client.crosshairTarget;
            getCast(cast);
            oldTooltipFlow = tooltipFlow;
            tooltipFlow = !frameStack.isEmpty() ? MathHelper.lerp(0.25f, tooltipFlow, 1) :
                    MathHelper.lerp(0.1f, tooltipFlow, 0);
            if (Math.abs(tooltipFlow) < 1.0E-5F) tooltipFlow = 0;
        });
    }

    private static void inGameTooltips() {
        HudRenderCallback.EVENT.register((matrices, delta) -> {
            if (MinecraftClient.getInstance().currentScreen == null) {
                var client = MinecraftClient.getInstance();

                if (!frameStack.isEmpty()) {
                    float flow = MathHelper.lerp(client.getTickDelta(), oldTooltipFlow, tooltipFlow);
                    matrices.push();
                    matrices.translate(0, 0, -450);
                    matrices.scale(1, 1, 1);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1, 1, 1, Math.min(flow, 0.8f));
                    var list = DrawUtil.getFakeScreen().getTooltipFromItem(frameStack);
                    //list.add(AndromedaTexts.ITEM_IN_FRAME);
                    List<TooltipComponent> list1 = list.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());

                    frameStack.getTooltipData().ifPresent(datax -> list1.add(1, Utilities.supply(() -> {
                        TooltipComponent component = TooltipComponentCallback.EVENT.invoker().getComponent(datax);
                        if (component == null) component = TooltipComponent.of(datax);
                        return component;
                    })));

                    int j = 0;
                    for (TooltipComponent tooltipComponent : list1) {
                        j += tooltipComponent.getHeight();
                    }

                    DrawUtil.renderTooltipFromComponents(matrices, list1, ((client.getWindow().getScaledWidth() / 2f) - (flow * 15)) + 15, ((client.getWindow().getScaledHeight() - j) / 2f) + 12);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    RenderSystem.disableBlend();
                    matrices.pop();
                }
            }
        });
    }

    private static void getCast(HitResult cast) {
        if (cast != null) if (cast.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hitResult = (EntityHitResult) cast;
            if (hitResult.getEntity() instanceof ItemFrameEntity itemFrameEntity) {
                frameStack = itemFrameEntity.getHeldItemStack();
                return;
            }
        }
        frameStack = ItemStack.EMPTY;
    }
}
