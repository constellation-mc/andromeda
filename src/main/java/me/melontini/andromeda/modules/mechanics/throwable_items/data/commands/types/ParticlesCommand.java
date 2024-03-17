package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Selector;
import me.melontini.dark_matter.api.minecraft.data.ExtraCodecs;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class ParticlesCommand extends Command {

    public static final Codec<ParticlesCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            Selector.CODEC.fieldOf("selector").forGetter(ParticlesCommand::selector),
            Codec.BOOL.optionalFieldOf("item", false).forGetter(ParticlesCommand::item),
            ExtraCodecs.COLOR.optionalFieldOf("colors").forGetter(ParticlesCommand::colors),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, ParticlesCommand::new));

    private final Selector selector;
    private final boolean item;
    private final Optional<Integer> colors;

    public ParticlesCommand(Selector selector, boolean item, Optional<Integer> colors, Optional<LootCondition> condition) {
        super(condition);
        this.selector = selector;
        this.item = item;
        this.colors = colors;
    }

    @Override
    protected boolean execute(Context context) {
        ServerCommandSource source = selector.function().apply(context);
        if (source == null) return false;

        if (colors().isPresent() || item())
            sendParticlePacket(context.world(), source.getPosition(), item(), context.stack(), colors());
        return true;
    }

    @Override
    public CommandType type() {
        return CommandType.PARTICLES;
    }

    public static void sendParticlePacket(ServerWorld world, Vec3d pos, boolean item, ItemStack stack, Optional<Integer> color) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeDouble(pos.getX()).writeDouble(pos.getY()).writeDouble(pos.getZ());
        byteBuf.writeBoolean(item);
        byteBuf.writeItemStack(stack);
        byteBuf.writeBoolean(color.isPresent());
        byteBuf.writeVarInt(color.orElse(-1));
        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
            ServerPlayNetworking.send(serverPlayerEntity, Main.FLYING_STACK_LANDED, byteBuf);
        }
    }
}
