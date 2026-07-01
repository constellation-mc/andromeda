package dev.zenfyr.andromeda.modules.gui.item_frame_tooltips;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.zenfyr.pulsar.api.util.Utilities;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector2i;

public class ItemFrameTooltipsClient {

  private Supplier<List<ClientTooltipComponent>> action;
  private float tooltipFlow;
  private float oldTooltipFlow;

  ItemFrameTooltipsClient() {
    inGameTooltips();

    ClientTickEvents.START_CLIENT_TICK.register(client -> {
      var cast = client.hitResult;
      getCast(cast);
      oldTooltipFlow = tooltipFlow;
      tooltipFlow =
          action != null ? Mth.lerp(0.25f, tooltipFlow, 1) : Mth.lerp(0.1f, tooltipFlow, 0);
      if (Math.abs(tooltipFlow) < 1.0E-5F) tooltipFlow = 0;
    });
  }

  public static void registerEntityTooltip(
      Predicate<EntityHitResult> predicate,
      Function<EntityHitResult, List<ClientTooltipComponent>> function) {
    ENTITY_LOOKUP.put(predicate, function);
  }

  private void inGameTooltips() {
    HudRenderCallback.EVENT.register((context, delta) -> {
      if (Minecraft.getInstance().screen == null) {
        var client = Minecraft.getInstance();

        if (action != null) {
          renderFromComponents(client, context, action.get());
        }
      }
    });

    registerEntityTooltip(
        entityHitResult -> entityHitResult.getEntity() instanceof ItemFrame ife
            && !ife.getItem().isEmpty(),
        entityHitResult -> {
          var frameStack = ((ItemFrame) entityHitResult.getEntity()).getItem();
          if (frameStack.isEmpty()) return Collections.emptyList();

          var list = Screen.getTooltipFromItem(Minecraft.getInstance(), frameStack);
          List<ClientTooltipComponent> components = list.stream()
              .map(Component::getVisualOrderText)
              .map(ClientTooltipComponent::create)
              .collect(Collectors.toCollection(ArrayList::new));

          frameStack
              .getTooltipImage()
              .ifPresent(datax -> components.add(1, Utilities.supply(() -> {
                ClientTooltipComponent component =
                    TooltipComponentCallback.EVENT.invoker().getComponent(datax);
                if (component == null) component = ClientTooltipComponent.create(datax);
                return component;
              })));
          return components;
        });
  }

  private static final Map<
          Predicate<EntityHitResult>, Function<EntityHitResult, List<ClientTooltipComponent>>>
      ENTITY_LOOKUP = new Reference2ObjectOpenHashMap<>();

  private void getCast(HitResult cast) {
    if (cast != null)
      if (cast.getType() == HitResult.Type.ENTITY) {
        EntityHitResult hitResult = (EntityHitResult) cast;
        var opt = ENTITY_LOOKUP.entrySet().stream()
            .filter(p -> p.getKey().test(hitResult))
            .findFirst();
        if (opt.isPresent()) {
          action = () -> opt.get().getValue().apply(hitResult);
          return;
        }
      }
    action = null;
  }

  private void renderFromComponents(
      Minecraft client, GuiGraphics context, List<ClientTooltipComponent> components) {
    if (components.isEmpty()) return;

    float flow = Mth.lerp(client.getFrameTime(), oldTooltipFlow, tooltipFlow);
    PoseStack matrices = context.pose();

    matrices.pushPose();
    matrices.translate(0, 0, -450);
    matrices.scale(1, 1, 1);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.setShaderColor(1, 1, 1, Math.min(flow, 0.8f));

    context.renderTooltipInternal(
        client.font, components, 0, 0, (screenWidth, screenHeight, sameX, sameY, width, height) -> {
          float smoothX = ((screenWidth / 2f) - (flow * 15)) + 27;
          float smoothY = ((client.getWindow().getGuiScaledHeight() - height) / 2f);
          matrices.translate(smoothX - (int) smoothX, smoothY - (int) smoothY, 1);
          return new Vector2i((int) smoothX, (int) smoothY);
        });
    RenderSystem.setShaderColor(1, 1, 1, 1);
    RenderSystem.disableBlend();
    matrices.popPose();
  }
}
