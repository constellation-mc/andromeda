package me.melontini.andromeda.modules.mechanics.throwable_items.packets;

import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;

import java.util.List;

public record ItemBehaviorsPayload(List<Item> items) implements CustomPayload {
    public static final Id<ItemBehaviorsPayload> ID = new Id<>(Main.ITEMS_WITH_BEHAVIORS);
    public static final PacketCodec<RegistryByteBuf, ItemBehaviorsPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.<RegistryByteBuf, Item>toList().apply(PacketCodecs.registryValue(RegistryKeys.ITEM)), ItemBehaviorsPayload::items,
            ItemBehaviorsPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
