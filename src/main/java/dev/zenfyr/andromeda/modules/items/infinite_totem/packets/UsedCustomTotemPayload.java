package dev.zenfyr.andromeda.modules.items.infinite_totem.packets;

import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemMain;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record UsedCustomTotemPayload(UUID uuid, ItemStack stack, ParticleOptions particle)
    implements CustomPacketPayload {

  public static final Type<UsedCustomTotemPayload> ID =
      new Type<>(InfiniteTotemMain.USED_CUSTOM_TOTEM);
  public static final StreamCodec<RegistryFriendlyByteBuf, UsedCustomTotemPayload> CODEC =
      StreamCodec.composite(
          MiscUtil.UUID_PACKET_CODEC,
          UsedCustomTotemPayload::uuid,
          ItemStack.STREAM_CODEC,
          UsedCustomTotemPayload::stack,
          ParticleTypes.STREAM_CODEC,
          UsedCustomTotemPayload::particle,
          UsedCustomTotemPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }
}
