package dev.zenfyr.andromeda.modules.items.pouches.entities;

import com.mojang.serialization.Codec;
import dev.zenfyr.andromeda.common.Andromeda;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record CustomPouchComponent(ResourceKey<LootTable> key) {

  public static final CustomPouchComponent DEFAULT = new CustomPouchComponent(
      ResourceKey.create(Registries.LOOT_TABLE, Andromeda.id("pouches/seeds")));
  public static final Codec<CustomPouchComponent> CODEC = ResourceKey.codec(Registries.LOOT_TABLE)
      .fieldOf("loot_table")
      .xmap(CustomPouchComponent::new, CustomPouchComponent::key)
      .codec();
  public static final StreamCodec<ByteBuf, CustomPouchComponent> PACKET_CODEC =
      ResourceKey.streamCodec(Registries.LOOT_TABLE)
          .map(CustomPouchComponent::new, CustomPouchComponent::key);
}
