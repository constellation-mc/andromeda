package dev.zenfyr.andromeda.common.util;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
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

  public static List<ItemStack> prepareLoot(
      @NonNull Level level, @NonNull ResourceKey<LootTable> lootId) {
    return level
        .getServer()
        .reloadableRegistries()
        .lookup()
        .lookup(Registries.LOOT_TABLE)
        .flatMap(reg -> reg.get(lootId))
        .map(Holder.Reference::value)
        .<List<ItemStack>>map(loot -> loot.getRandomItems(
            new LootParams.Builder(((ServerLevel) level)).create(LootContextParamSets.EMPTY)))
        .orElse(List.of());
  }
}
