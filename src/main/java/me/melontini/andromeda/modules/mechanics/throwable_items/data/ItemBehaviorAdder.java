package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.function.Supplier;

public class ItemBehaviorAdder {

    public static ItemBehavior dataPack(ItemBehaviorData data) {
        return (stack, fie, world, user, hitResult) -> {//default behavior to handle datapacks
            data.commands().shuffle().stream().findFirst().ifPresent(commands -> {
                switch (hitResult.getType()) {
                    case ENTITY -> executeCommands(world, fie, user, hitResult, commands.on_entity());
                    case BLOCK -> executeCommands(world, fie, user, hitResult, commands.on_block());
                    case MISS -> executeCommands(world, fie, user, hitResult, commands.on_miss());
                }
                executeCommands(world, fie, user, hitResult, commands.on_any());
            });


            var particles = data.particles();
            sendParticlePacket(fie, fie.getPos(), particles.item(), stack, particles.colors());
        };
    }

    private static ServerCommandSource forEntity(ServerWorld world, Entity entity) {
        return new ServerCommandSource(
                world.getServer(), entity.getPos(),
                new Vec2f(entity.getPitch(), entity.getYaw()),
                world, 4, entity.getEntityName(), entity.getName(),
                world.getServer(), entity);
    }

    private static void executeCommands(ServerWorld world, FlyingItemEntity fie, Entity user, HitResult hitResult, ItemBehaviorData.Commands.Holder data) {
        if (data == ItemBehaviorData.Commands.Holder.EMPTY) return;

        executeCommands(world, data.item(), () -> forEntity(world, fie).withSilent());
        executeCommands(world, data.user(), () -> forEntity(world, user).withSilent());
        executeCommands(world, data.server(), () -> world.getServer().getCommandSource().withSilent());

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity) {
                executeCommands(world, data.hit_entity(), () -> forEntity(world, entity).withSilent());
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) hitResult;
            executeCommands(world, data.hit_block(), () -> new ServerCommandSource(
                    world.getServer(),
                    new Vec3d(hit.getBlockPos().getX(),
                            hit.getBlockPos().getY(),
                            hit.getBlockPos().getZ()), Vec2f.ZERO,
                    world, 4, "Block", TextUtil.literal("Block"),
                    world.getServer(), fie).withSilent());
        }
    }

    private static void executeCommands(ServerWorld world, Collection<String> commands, Supplier<ServerCommandSource> source) {
        if (commands == null || commands.isEmpty()) return;

        var s = source.get();
        for (String command : commands) {
            world.getServer().getCommandManager().executeWithPrefix(s, command);
        }
    }

    public static void sendParticlePacket(FlyingItemEntity flyingItemEntity, Vec3d pos, boolean item, ItemStack stack, int color) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeDouble(pos.getX()).writeDouble(pos.getY()).writeDouble(pos.getZ());
        byteBuf.writeBoolean(item);
        byteBuf.writeItemStack(stack);
        byteBuf.writeBoolean(color != -1);
        byteBuf.writeVarInt(color);
        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(flyingItemEntity)) {
            ServerPlayNetworking.send(serverPlayerEntity, Main.FLYING_STACK_LANDED, byteBuf);
        }
    }
}
