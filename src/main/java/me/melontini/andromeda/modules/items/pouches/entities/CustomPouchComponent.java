package me.melontini.andromeda.modules.items.pouches.entities;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import me.melontini.andromeda.common.Andromeda;
import net.minecraft.loot.LootTable;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record CustomPouchComponent(RegistryKey<LootTable> key) {

    public static final CustomPouchComponent DEFAULT = new CustomPouchComponent(RegistryKey.of(RegistryKeys.LOOT_TABLE, Andromeda.id("pouches/seeds")));
    public static final Codec<CustomPouchComponent> CODEC = RegistryKey.createCodec(RegistryKeys.LOOT_TABLE)
            .fieldOf("loot_table").xmap(CustomPouchComponent::new, CustomPouchComponent::key).codec();
    public static final PacketCodec<ByteBuf, CustomPouchComponent> PACKET_CODEC = RegistryKey.createPacketCodec(RegistryKeys.LOOT_TABLE)
            .xmap(CustomPouchComponent::new, CustomPouchComponent::key);
}
