package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.util.Debug;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public abstract class Command {

    protected final List<String> commands;
    protected final ItemBehaviorData.Particles particles;
    protected final Optional<LootCondition> condition;

    public boolean execute(Context context) {
        if (!this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) return false;

        ServerCommandSource source = createSource(context);
        if (source == null) return false;
        if (!Debug.Keys.PRINT_DATA_COMMAND_OUTPUT.isPresent()) source = source.withSilent();

        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                context.world().getServer().getCommandManager().executeWithPrefix(source, command);
            }
        }

        if (particles.colors().isPresent() || particles.item())
            sendParticlePacket(context.world(), source.getPosition(), particles.item(), context.stack(), particles.colors());
        return true;
    }

    protected abstract @Nullable ServerCommandSource createSource(Context context);
    public abstract CommandType type();

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
