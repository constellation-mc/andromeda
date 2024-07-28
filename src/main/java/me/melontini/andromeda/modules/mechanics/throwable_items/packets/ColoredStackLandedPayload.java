package me.melontini.andromeda.modules.mechanics.throwable_items.packets;

import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;


public record ColoredStackLandedPayload(ItemStack dye) implements CustomPayload {
    public static final Id<ColoredStackLandedPayload> ID = new Id<>(Main.COLORED_FLYING_STACK_LANDED);
    public static final PacketCodec<RegistryByteBuf, ColoredStackLandedPayload> CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, ColoredStackLandedPayload::dye,
            ColoredStackLandedPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
