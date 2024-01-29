package me.melontini.andromeda.modules.misc.unknown.client;

import me.melontini.andromeda.modules.misc.unknown.Main;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class Client {

    Client() {
        Main.ROSE_OF_THE_VALLEY_BLOCK.ifPresent(b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));
    }
}
