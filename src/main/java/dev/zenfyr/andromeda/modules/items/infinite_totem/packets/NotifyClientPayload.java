package dev.zenfyr.andromeda.modules.items.infinite_totem.packets;

import dev.zenfyr.andromeda.modules.items.infinite_totem.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record NotifyClientPayload(int entity, ItemStack stack) implements CustomPacketPayload {
  public static final Type<NotifyClientPayload> ID = new Type<>(Main.NOTIFY_CLIENT);
  public static final StreamCodec<RegistryFriendlyByteBuf, NotifyClientPayload> CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          NotifyClientPayload::entity,
          ItemStack.OPTIONAL_STREAM_CODEC,
          NotifyClientPayload::stack,
          NotifyClientPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }
}
