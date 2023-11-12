package me.melontini.andromeda.modules.misc.unknown;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class UnknownClient {

    public static void init() {
        UnknownContent.ROSE_OF_THE_VALLEY_BLOCK.ifPresent(b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));
    }
}
