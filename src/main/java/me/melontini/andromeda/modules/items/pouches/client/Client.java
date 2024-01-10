package me.melontini.andromeda.modules.items.pouches.client;

import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.modules.items.pouches.Content;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.Debug;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class Client {

    public static void init() {
        EntityRendererRegistry.register(Content.POUCH.orThrow(), FlyingItemEntityRenderer::new);

        if (Debug.hasKey(Debug.Keys.PRINT_DEBUG_MESSAGES)) {
            StringBuilder b = new StringBuilder();
            b.append("Viewable block entities:");
            Content.VIEWABLE_BLOCKS.forEach((blockEntityType, field) -> {
                b.append('\n').append(CommonRegistries.blockEntityTypes().getId(blockEntityType)).append(": ").append(field.getName());
            });
            AndromedaLog.info(b);
        }
    }
}
