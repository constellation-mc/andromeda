package dev.zenfyr.andromeda.modules.entities.boats.client;

import dev.zenfyr.andromeda.modules.entities.boats.BoatEntities;
import dev.zenfyr.andromeda.modules.entities.boats.entities.TNTBoatEntity;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;

public class BoatsClient {

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

  public static void sendExplodePacket(TNTBoatEntity entity) {
    FriendlyByteBuf buf = PacketByteBufs.create().writeUUID(entity.getUUID());
    ClientPlayNetworking.send(TNTBoatEntity.EXPLODE_BOAT_ON_SERVER, buf);
  }
}
