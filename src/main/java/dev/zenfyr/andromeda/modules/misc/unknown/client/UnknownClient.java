package dev.zenfyr.andromeda.modules.misc.unknown.client;

import dev.zenfyr.andromeda.modules.misc.unknown.RoseOfTheValley;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class UnknownClient {

  public static void onClient() {
    BlockRenderLayerMap.putBlocks(
        ChunkSectionLayer.CUTOUT, RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow());
  }
}
