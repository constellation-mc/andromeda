package dev.zenfyr.andromeda.modules.misc.unknown.client;

import dev.zenfyr.andromeda.modules.misc.unknown.RoseOfTheValley;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class UnknownClient {

  public static void onClient() {
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderType.cutout(), RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow());
  }
}
