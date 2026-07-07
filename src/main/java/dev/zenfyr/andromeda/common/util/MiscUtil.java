package dev.zenfyr.andromeda.common.util;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

public class MiscUtil {

  public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

  public static final StreamCodec<ByteBuf, UUID> UUID_PACKET_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_LONG,
      UUID::getMostSignificantBits,
      ByteBufCodecs.VAR_LONG,
      UUID::getLeastSignificantBits,
      UUID::new);

  public static double horizontalDistanceTo(Vec3 owner, Vec3 target) {
    double d = target.x - owner.x;
    double f = target.z - owner.z;
    return Math.sqrt(d * d + f * f);
  }

  public static BlockPos vec3dAsBlockPos(Vec3 vec3d) {
    return new BlockPos(Mth.floor(vec3d.x), Mth.floor(vec3d.y), Mth.floor(vec3d.z));
  }

  public static LootTable getLootTable(Level level, ResourceKey<LootTable> key) {
    return level.getServer().reloadableRegistries().getLootTable(key);
  }
}
