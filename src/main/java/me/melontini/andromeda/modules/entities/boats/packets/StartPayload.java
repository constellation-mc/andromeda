package me.melontini.andromeda.modules.entities.boats.packets;

import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.entities.boats.client.ClientSoundHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record StartPayload(UUID entity, ItemStack record) implements CustomPayload {
    public static final Id<StartPayload> ID = new Id<>(ClientSoundHolder.JUKEBOX_START_PLAYING);
    public static final PacketCodec<RegistryByteBuf, StartPayload> CODEC = PacketCodec.tuple(
            MiscUtil.UUID_PACKET_CODEC, StartPayload::entity,
            ItemStack.PACKET_CODEC, StartPayload::record,
            StartPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
