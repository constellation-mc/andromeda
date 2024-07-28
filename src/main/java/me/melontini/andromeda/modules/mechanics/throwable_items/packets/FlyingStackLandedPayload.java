package me.melontini.andromeda.modules.mechanics.throwable_items.packets;

import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.joml.Vector3f;

import java.util.Optional;

public record FlyingStackLandedPayload(Vector3f pos, Optional<ItemStack> stack, Optional<Integer> color) implements CustomPayload {
    public static final Id<FlyingStackLandedPayload> ID = new Id<>(Main.FLYING_STACK_LANDED);
    public static final PacketCodec<RegistryByteBuf, FlyingStackLandedPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VECTOR3F, FlyingStackLandedPayload::pos,
            PacketCodecs.optional(ItemStack.PACKET_CODEC), FlyingStackLandedPayload::stack,
            PacketCodecs.optional(PacketCodecs.VAR_INT), FlyingStackLandedPayload::color,
            FlyingStackLandedPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
