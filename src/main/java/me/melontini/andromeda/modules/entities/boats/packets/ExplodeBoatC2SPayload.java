package me.melontini.andromeda.modules.entities.boats.packets;

import java.util.UUID;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.entities.boats.entities.TNTBoatEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ExplodeBoatC2SPayload(UUID entity) implements CustomPayload {
  public static final Id<ExplodeBoatC2SPayload> ID = new Id<>(TNTBoatEntity.EXPLODE_BOAT_ON_SERVER);
  public static final PacketCodec<RegistryByteBuf, ExplodeBoatC2SPayload> CODEC = PacketCodec.tuple(
      MiscUtil.UUID_PACKET_CODEC, ExplodeBoatC2SPayload::entity, ExplodeBoatC2SPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
