package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.command.CommandType;
import me.melontini.commander.api.command.Selector;
import me.melontini.commander.api.event.EventContext;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public record ParticleCommand(Selector.Conditioned selector, boolean item, Optional<Integer> colors) implements Command {

    public static final Codec<ParticleCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            Selector.CODEC.fieldOf("selector").forGetter(ParticleCommand::selector),
            ExtraCodecs.optional("item", Codec.BOOL, true).forGetter(ParticleCommand::item),
            ExtraCodecs.optional("colors", ExtraCodecs.COLOR).forGetter(ParticleCommand::colors)
    ).apply(data, ParticleCommand::new));

    @Override
    public boolean execute(EventContext context) {
        var opt = selector.select(context);
        if (opt.isEmpty()) return false;

        sendParticlePacket(opt.get().getWorld(), opt.get().getPosition(), context.lootContext().get(LootContextParameters.TOOL));
        return true;
    }

    @Override
    public CommandType type() {
        return Main.PARTICLE_COMMAND.orThrow();
    }

    public void sendParticlePacket(ServerWorld world, Vec3d pos, ItemStack stack) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeDouble(pos.getX()).writeDouble(pos.getY()).writeDouble(pos.getZ());
        byteBuf.writeBoolean(item);
        byteBuf.writeItemStack(stack);
        byteBuf.writeBoolean(colors.isPresent());
        byteBuf.writeVarInt(colors.orElse(-1));
        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
            ServerPlayNetworking.send(serverPlayerEntity, Main.FLYING_STACK_LANDED, byteBuf);
        }
    }
}
