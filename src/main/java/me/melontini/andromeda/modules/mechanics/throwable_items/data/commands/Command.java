package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public abstract class Command {

    protected final List<String> commands;
    protected final ItemBehaviorData.Particles particles;

    public void execute(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult) {
        ServerCommandSource source = createSource(stack, fie, world, user, hitResult);
        if (source == null) return;

        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                world.getServer().getCommandManager().executeWithPrefix(source, command);
            }
        }

        if (particles.colors().isPresent() || particles.item())
            sendParticlePacket(fie, fie.getPos(), particles.item(), stack, particles.colors());
    }

    protected abstract @Nullable ServerCommandSource createSource(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult);
    public abstract CommandType type();

    public static void sendParticlePacket(FlyingItemEntity flyingItemEntity, Vec3d pos, boolean item, ItemStack stack, Optional<Integer> color) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeDouble(pos.getX()).writeDouble(pos.getY()).writeDouble(pos.getZ());
        byteBuf.writeBoolean(item);
        byteBuf.writeItemStack(stack);
        byteBuf.writeBoolean(color.isPresent());
        byteBuf.writeVarInt(color.orElse(-1));
        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(flyingItemEntity)) {
            ServerPlayNetworking.send(serverPlayerEntity, Main.FLYING_STACK_LANDED, byteBuf);
        }
    }
}
