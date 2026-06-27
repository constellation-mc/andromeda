package dev.zenfyr.andromeda.modules.gui.item_frame_tooltips;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.client.GlobalAlphaController;
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
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2i;

public class Client {

  public static final Identifier TOOLTIP_HUD = Andromeda.id("tooltip_hud");
  private Supplier<List<ClientTooltipComponent>> action;
  private float tooltipFlow;
  private float oldTooltipFlow;

  Client() {
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
    HudElementRegistry.attachElementAfter(
        VanillaHudElements.CROSSHAIR, TOOLTIP_HUD, (context, tickCounter) -> {
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
                    ClientTooltipComponentCallback.EVENT.invoker().getClientComponent(datax);
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
      Minecraft client, GuiGraphicsExtractor context, List<ClientTooltipComponent> components) {
    if (components.isEmpty()) return;

    float flow = Mth.lerp(
        client.getDeltaTracker().getGameTimeDeltaPartialTick(false), oldTooltipFlow, tooltipFlow);
    Matrix3x2fStack matrices = context.pose();
    matrices.pushMatrix();

    try {
      GlobalAlphaController.MODIFIER.set(key -> {
        float flowAlpha = Math.min(flow, 0.8f);
        return key * flowAlpha;
      });

      context.tooltip(
          client.font,
          components,
          0,
          0,
          (screenWidth, screenHeight, sameX, sameY, width, height) -> {
            float smoothX = ((screenWidth / 2f) - (flow * 15)) + 27;
            int y = (client.getWindow().getGuiScaledHeight() - height) / 2;
            matrices.translate(smoothX - (int) smoothX, 0);
            return new Vector2i((int) smoothX, y);
          },
          null);
    } finally {
      GlobalAlphaController.MODIFIER.remove();
    }
    matrices.popMatrix();
  }
}
