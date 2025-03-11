package me.melontini.andromeda.common.util;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MiscUtil {

  public static double horizontalDistanceTo(Vec3d owner, Vec3d target) {
    double d = target.x - owner.x;
    double f = target.z - owner.z;
    return Math.sqrt(d * d + f * f);
  }

  public static BlockPos vec3dAsBlockPos(Vec3d vec3d) {
    return new BlockPos(
        MathHelper.floor(vec3d.x), MathHelper.floor(vec3d.y), MathHelper.floor(vec3d.z));
  }

  public static void crudeSetVelocity(Entity entity, double x, double y, double z) {
    crudeSetVelocity(entity, new Vec3d(x, y, z));
  }

  public static void crudeSetVelocity(Entity entity, Vec3d velocity) {
    if (!entity.world.isClient) {
      entity.setVelocity(velocity);
      for (ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
      }
    } else {
      throw new UnsupportedOperationException(
          "Can't send packets to client unless you're on server.");
    }
  }
}
