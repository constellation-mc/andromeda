package dev.zenfyr.andromeda.modules.items.infinite_totem.packets;

import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StartAscensionPayload(int item, int pair, boolean start)
    implements CustomPacketPayload {
  public static final Type<StartAscensionPayload> ID =
      new Type<>(InfiniteTotemMain.START_ASCENSION);
  public static final StreamCodec<RegistryFriendlyByteBuf, StartAscensionPayload> CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          StartAscensionPayload::item,
          ByteBufCodecs.VAR_INT,
          StartAscensionPayload::pair,
          ByteBufCodecs.BOOL,
          StartAscensionPayload::start,
          StartAscensionPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }
}
