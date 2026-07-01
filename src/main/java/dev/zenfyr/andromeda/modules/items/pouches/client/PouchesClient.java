package dev.zenfyr.andromeda.modules.items.pouches.client;

import dev.zenfyr.andromeda.modules.items.pouches.PouchesMain;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class PouchesClient {

  public static void init() {
    EntityRendererRegistry.register(PouchesMain.POUCH.orThrow(), ThrownItemRenderer::new);
  }
}
