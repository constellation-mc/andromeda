package me.melontini.andromeda.modules.entities.boats.client;

import java.util.Map;
import me.melontini.andromeda.modules.entities.boats.BoatEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.util.math.Direction;

public class Client {

  public static void init() {
    var map = Map.of(
        BoatEntities.BOAT_WITH_FURNACE,
        Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, Direction.NORTH),
        BoatEntities.BOAT_WITH_JUKEBOX,
        Blocks.JUKEBOX.getDefaultState(),
        BoatEntities.BOAT_WITH_TNT,
        Blocks.TNT.getDefaultState(),
        BoatEntities.BOAT_WITH_HOPPER,
        Blocks.HOPPER.getDefaultState());

    map.forEach((keeper, blockState) -> {
      if (keeper.isPresent()) {
        EntityRendererRegistry.register(
            keeper.get(), ctx -> new BoatWithBlockRenderer(ctx, blockState));
      }
    });

    if (BoatEntities.BOAT_WITH_JUKEBOX.isPresent()) ClientSoundHolder.init();
  }
}
