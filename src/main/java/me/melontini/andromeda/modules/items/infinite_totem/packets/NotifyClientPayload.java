package me.melontini.andromeda.modules.items.infinite_totem.packets;

import me.melontini.andromeda.modules.items.infinite_totem.Main;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record NotifyClientPayload(int entity, ItemStack stack) implements CustomPayload {
    public static final Id<NotifyClientPayload> ID = new Id<>(Main.NOTIFY_CLIENT);
    public static final PacketCodec<RegistryByteBuf, NotifyClientPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, NotifyClientPayload::entity,
            ItemStack.OPTIONAL_PACKET_CODEC, NotifyClientPayload::stack,
            NotifyClientPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
