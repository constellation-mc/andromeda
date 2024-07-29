package me.melontini.andromeda.modules.items.infinite_totem.packets;

import java.util.UUID;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.items.infinite_totem.Main;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public record UsedCustomTotemPayload(UUID uuid, ItemStack stack, ParticleEffect particle)
    implements CustomPayload {

  public static final Id<UsedCustomTotemPayload> ID = new Id<>(Main.USED_CUSTOM_TOTEM);
  public static final PacketCodec<RegistryByteBuf, UsedCustomTotemPayload> CODEC =
      PacketCodec.tuple(
          MiscUtil.UUID_PACKET_CODEC,
          UsedCustomTotemPayload::uuid,
          ItemStack.PACKET_CODEC,
          UsedCustomTotemPayload::stack,
          ParticleTypes.PACKET_CODEC,
          UsedCustomTotemPayload::particle,
          UsedCustomTotemPayload::new);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
