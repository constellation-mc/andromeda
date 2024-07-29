package me.melontini.andromeda.common.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class MiscUtil {

  public static final PacketCodec<RegistryByteBuf, UUID> UUID_PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_LONG, UUID::getMostSignificantBits, PacketCodecs.VAR_LONG, UUID::getLeastSignificantBits, UUID::new);

    public static double horizontalDistanceTo(Vec3d owner, Vec3d target) {
        double d = target.x - owner.x;
        double f = target.z - owner.z;
        return Math.sqrt(d * d + f * f);
    }

  public static BlockPos vec3dAsBlockPos(Vec3d vec3d) {
    return new BlockPos(
        MathHelper.floor(vec3d.x), MathHelper.floor(vec3d.y), MathHelper.floor(vec3d.z));
  }
}
