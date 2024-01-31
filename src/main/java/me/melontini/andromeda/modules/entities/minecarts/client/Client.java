package me.melontini.andromeda.modules.entities.minecarts.client;

import me.melontini.andromeda.modules.entities.boats.client.ClientSoundHolder;
import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;

public class Client {

    Client() {
        MinecartEntities.ANVIL_MINECART_ENTITY.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART)));
        MinecartEntities.NOTEBLOCK_MINECART_ENTITY.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART)));
        MinecartEntities.JUKEBOX_MINECART_ENTITY.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new MinecartEntityRenderer<>(ctx, EntityModelLayers.MINECART)));

        MinecartEntities.JUKEBOX_MINECART_ENTITY.ifPresent(type -> ClientSoundHolder.init());
    }
}
