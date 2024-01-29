package me.melontini.andromeda.modules.blocks.incubator.client;

import me.melontini.andromeda.modules.blocks.incubator.Main;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class Client {

    Client() {
        Main.INCUBATOR_BLOCK.ifPresent(b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));
        Main.INCUBATOR_BLOCK_ENTITY.ifPresent(b -> BlockEntityRendererFactories.register(b, IncubatorBlockRenderer::new));
    }
}
