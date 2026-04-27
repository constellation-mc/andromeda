package dev.zenfyr.andromeda.modules.entities.boats.packets;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.common.util.MiscUtil;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record RecordPlaybackS2CPayload(UUID entity, ItemStack record)
    implements CustomPacketPayload {
  public static final Type<RecordPlaybackS2CPayload> ID = new Type<>(id("record_playback"));

  public static final StreamCodec<RegistryFriendlyByteBuf, RecordPlaybackS2CPayload> CODEC =
      StreamCodec.composite(
          MiscUtil.UUID_PACKET_CODEC,
          RecordPlaybackS2CPayload::entity,
          ItemStack.OPTIONAL_STREAM_CODEC,
          RecordPlaybackS2CPayload::record,
          RecordPlaybackS2CPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }
}
