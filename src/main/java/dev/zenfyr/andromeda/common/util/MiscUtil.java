package dev.zenfyr.andromeda.common.util;

import com.google.gson.*;
import java.util.List;
import lombok.NonNull;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

public class MiscUtil {

  public static double horizontalDistanceTo(Vec3 owner, Vec3 target) {
    double d = target.x - owner.x;
    double f = target.z - owner.z;
    return Math.sqrt(d * d + f * f);
  }

  public static BlockPos vec3dAsBlockPos(Vec3 vec3d) {
    return new BlockPos(Mth.floor(vec3d.x), Mth.floor(vec3d.y), Mth.floor(vec3d.z));
  }

  public static void crudeSetVelocity(Entity entity, double x, double y, double z) {
    crudeSetVelocity(entity, new Vec3(x, y, z));
  }

  public static void crudeSetVelocity(Entity entity, Vec3 velocity) {
    if (!entity.level.isClientSide) {
      entity.setDeltaMovement(velocity);
      for (ServerPlayer player : PlayerLookup.tracking(entity)) {
        player.connection.send(new ClientboundSetEntityMotionPacket(entity));
      }
    } else {
      throw new UnsupportedOperationException(
          "Can't send packets to client unless you're on server.");
    }
  }

  public static List<ItemStack> prepareLoot(
      @NonNull Level world, @NonNull ResourceLocation lootId) {
    return ((ServerLevel) world)
        .getServer()
        .getLootData()
        .getLootTable(lootId)
        .getRandomItems(
            new LootParams.Builder(((ServerLevel) world)).create(LootContextParamSets.EMPTY));
  }
}
