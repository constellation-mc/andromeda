package dev.zenfyr.andromeda.modules.entities.boats.packets;

import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.entities.boats.entities.TNTBoatEntity;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ExplodeBoatC2SPayload(UUID entity) implements CustomPacketPayload {
  public static final Type<ExplodeBoatC2SPayload> ID =
      new Type<>(TNTBoatEntity.EXPLODE_BOAT_ON_SERVER);
  public static final StreamCodec<ByteBuf, ExplodeBoatC2SPayload> CODEC = StreamCodec.composite(
      MiscUtil.UUID_PACKET_CODEC, ExplodeBoatC2SPayload::entity, ExplodeBoatC2SPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }
}
