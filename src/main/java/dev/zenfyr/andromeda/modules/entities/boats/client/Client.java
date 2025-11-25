package dev.zenfyr.andromeda.modules.entities.boats.client;

import java.util.Map;
import dev.zenfyr.andromeda.modules.entities.boats.BoatEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;

public class Client {

  public static void init() {
    var map = Map.of(
        BoatEntities.BOAT_WITH_FURNACE,
        Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH),
        BoatEntities.BOAT_WITH_JUKEBOX,
        Blocks.JUKEBOX.defaultBlockState(),
        BoatEntities.BOAT_WITH_TNT,
        Blocks.TNT.defaultBlockState(),
        BoatEntities.BOAT_WITH_HOPPER,
        Blocks.HOPPER.defaultBlockState());

    map.forEach((keeper, blockState) -> {
      if (keeper.isPresent()) {
        EntityRendererRegistry.register(
            keeper.get(), ctx -> new BoatWithBlockRenderer(ctx, blockState));
      }
    });

    if (BoatEntities.BOAT_WITH_JUKEBOX.isPresent()) ClientSoundHolder.init();
  }
}
