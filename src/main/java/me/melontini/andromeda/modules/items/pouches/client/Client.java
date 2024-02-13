package me.melontini.andromeda.modules.items.pouches.client;

import me.melontini.andromeda.modules.items.pouches.Main;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class Client {

    Client() {
        EntityRendererRegistry.register(Main.POUCH.orThrow(), FlyingItemEntityRenderer::new);
    }
}
