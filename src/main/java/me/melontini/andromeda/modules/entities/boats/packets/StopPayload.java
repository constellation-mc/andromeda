package me.melontini.andromeda.modules.entities.boats.packets;

import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.MiscUtil;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record StopPayload(UUID entity) implements CustomPayload {
    public static final Id<StopPayload> ID = new Id<>(Andromeda.id("jukebox_stop_playing"));
    public static final PacketCodec<RegistryByteBuf, StopPayload> CODEC = PacketCodec.tuple(
            MiscUtil.UUID_PACKET_CODEC, StopPayload::entity,
            StopPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
