package me.melontini.andromeda.modules.blocks.incubator.client;

import me.melontini.andromeda.modules.blocks.incubator.Content;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class Client {

    public static void init() {
        Content.INCUBATOR_BLOCK.ifPresent(b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));
        Content.INCUBATOR_BLOCK_ENTITY.ifPresent(b -> BlockEntityRendererFactories.register(b, IncubatorBlockRenderer::new));
    }
}
