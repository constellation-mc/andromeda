package me.melontini.andromeda.modules.entities.minecarts.client;

import static me.melontini.andromeda.modules.entities.minecarts.MinecartEntities.*;

import me.melontini.andromeda.modules.entities.boats.client.ClientSoundHolder;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;

public class Client {

  public static void init() {
    if (ANVIL_MINECART_ENTITY.isPresent()) {
      EntityRendererRegistry.register(
          ANVIL_MINECART_ENTITY.get(),
          ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART));
    }

    if (NOTEBLOCK_MINECART_ENTITY.isPresent()) {
      EntityRendererRegistry.register(
          NOTEBLOCK_MINECART_ENTITY.get(),
          ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART));
    }

    if (JUKEBOX_MINECART_ENTITY.isPresent()) {
      EntityRendererRegistry.register(
          JUKEBOX_MINECART_ENTITY.get(),
          ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART));
    }

    if (JUKEBOX_MINECART_ENTITY.isPresent()) ClientSoundHolder.init();
  }
}
