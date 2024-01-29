package me.melontini.andromeda.modules.gui.item_frame_tooltips.client;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Client {

    private static Supplier<List<TooltipComponent>> action;
    private static float tooltipFlow;
    private static float oldTooltipFlow;

    Client() {
        inGameTooltips();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            var cast = client.crosshairTarget;
            getCast(cast);
            oldTooltipFlow = tooltipFlow;
            tooltipFlow = action != null ? MathHelper.lerp(0.25f, tooltipFlow, 1) :
                    MathHelper.lerp(0.1f, tooltipFlow, 0);
            if (Math.abs(tooltipFlow) < 1.0E-5F) tooltipFlow = 0;
        });
    }

    public static void registerEntityTooltip(Predicate<EntityHitResult> predicate, Function<EntityHitResult, List<TooltipComponent>> function) {
        ENTITY_LOOKUP.put(predicate, function);
    }

    private static void inGameTooltips() {
        HudRenderCallback.EVENT.register((context, delta) -> {
            if (MinecraftClient.getInstance().currentScreen == null) {
                var client = MinecraftClient.getInstance();

                if (action != null) {
                    renderFromComponents(client, context, action.get());
                }
            }
        });

        registerEntityTooltip(entityHitResult -> entityHitResult.getEntity() instanceof ItemFrameEntity ife && !ife.getHeldItemStack().isEmpty(), entityHitResult -> {
            var frameStack = ((ItemFrameEntity) entityHitResult.getEntity()).getHeldItemStack();
            if (frameStack.isEmpty()) return Collections.emptyList();

            var list = Screen.getTooltipFromItem(MinecraftClient.getInstance(), frameStack);
            List<TooltipComponent> components = list.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());

            frameStack.getTooltipData().ifPresent(datax -> components.add(1, Utilities.supply(() -> {
                TooltipComponent component = TooltipComponentCallback.EVENT.invoker().getComponent(datax);
                if (component == null) component = TooltipComponent.of(datax);
                return component;
            })));
            return components;
        });
    }

    private static final Map<Predicate<EntityHitResult>, Function<EntityHitResult, List<TooltipComponent>>> ENTITY_LOOKUP = new Reference2ObjectOpenHashMap<>();

    private static void getCast(HitResult cast) {

        if (cast != null) if (cast.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hitResult = (EntityHitResult) cast;
            var opt = ENTITY_LOOKUP.entrySet().stream().filter(p -> p.getKey().test(hitResult)).findFirst();
            if (opt.isPresent()) {
                action = () -> opt.get().getValue().apply(hitResult);
                return;
            }
        }
        action = null;
    }

    private static void renderFromComponents(MinecraftClient client, DrawContext context, List<TooltipComponent> components) {
        if (components.isEmpty()) return;

        float flow = MathHelper.lerp(client.getTickDelta(), oldTooltipFlow, tooltipFlow);
        MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(0, 0, -450);
        matrices.scale(1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, Math.min(flow, 0.8f));

        int j = 0;
        for (TooltipComponent tooltipComponent : components) {
            j += tooltipComponent.getHeight();
        }

        DrawUtil.renderTooltipFromComponents(context, components, ((client.getWindow().getScaledWidth() / 2f) - (flow * 15)) + 15, ((client.getWindow().getScaledHeight() - j) / 2f) + 12);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        matrices.pop();
    }
}
