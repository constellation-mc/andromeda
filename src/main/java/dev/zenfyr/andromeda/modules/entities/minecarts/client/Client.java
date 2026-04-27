package dev.zenfyr.andromeda.modules.entities.minecarts.client;

import static dev.zenfyr.andromeda.modules.entities.minecarts.MinecartEntities.*;

import dev.zenfyr.andromeda.modules.entities.boats.client.ClientSoundHolder;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.MinecartRenderer;

public class Client {

  public static void init() {
    if (ANVIL_MINECART_ENTITY.isPresent()) {
      EntityRenderers.register(
          ANVIL_MINECART_ENTITY.get(), ctx -> new MinecartRenderer(ctx, ModelLayers.MINECART));
    }

    if (NOTEBLOCK_MINECART_ENTITY.isPresent()) {
      EntityRenderers.register(
          NOTEBLOCK_MINECART_ENTITY.get(), ctx -> new MinecartRenderer(ctx, ModelLayers.MINECART));
    }

    if (JUKEBOX_MINECART_ENTITY.isPresent()) {
      EntityRenderers.register(
          JUKEBOX_MINECART_ENTITY.get(), ctx -> new MinecartRenderer(ctx, ModelLayers.MINECART));
    }

    if (JUKEBOX_MINECART_ENTITY.isPresent()) ClientSoundHolder.init();
  }
}
