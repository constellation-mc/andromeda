package dev.zenfyr.andromeda.modules.items.pouches.client;

import dev.zenfyr.andromeda.modules.items.pouches.Main;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class Client {

  public static void init() {
    EntityRendererRegistry.register(Main.POUCH.orThrow(), ThrownItemRenderer::new);
  }
}
