package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.dark_matter.api.base.util.ColorUtil;
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
            switch (hitResult.getType()) {
                case ENTITY -> executeCommands(world, fie, user, hitResult, data.on_entity_hit());
                case BLOCK -> executeCommands(world, fie, user, hitResult, data.on_block_hit());
                case MISS -> executeCommands(world, fie, user, hitResult, data.on_miss());
            }
            executeCommands(world, fie, user, hitResult, data.on_any_hit());

            sendParticlePacket(fie, fie.getPos(), data.spawn_item_particles(), stack, data.spawn_colored_particles(),
                    ColorUtil.toColor(data.particle_colors().red(),
                            data.particle_colors().green(),
                            data.particle_colors().blue())
            );
        };
    }

    private static ServerCommandSource forEntity(ServerWorld world, Entity entity) {
        return new ServerCommandSource(
                world.getServer(), entity.getPos(),
                new Vec2f(entity.getPitch(), entity.getYaw()),
                world, 4, entity.getEntityName(), TextUtil.literal(entity.getEntityName()),
                world.getServer(), entity);
    }

    private static void executeCommands(ServerWorld world, FlyingItemEntity fie, Entity user, HitResult hitResult, ItemBehaviorData.CommandHolder data) {
        if (data == ItemBehaviorData.CommandHolder.EMPTY) return;

        executeCommands(world, data.item_commands(), () -> forEntity(world, fie).withSilent());
        executeCommands(world, data.user_commands(), () -> forEntity(world, user).withSilent());
        executeCommands(world, data.server_commands(), () -> world.getServer().getCommandSource().withSilent());

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity) {
                executeCommands(world, data.hit_entity_commands(), () -> forEntity(world, entity).withSilent());
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) hitResult;
            executeCommands(world, data.hit_block_commands(), () -> new ServerCommandSource(
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

    public static void sendParticlePacket(FlyingItemEntity flyingItemEntity, Vec3d pos, boolean item, ItemStack stack, boolean colored, int color) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeDouble(pos.getX()).writeDouble(pos.getY()).writeDouble(pos.getZ());
        byteBuf.writeBoolean(item);
        byteBuf.writeItemStack(stack);
        byteBuf.writeBoolean(colored);
        byteBuf.writeVarInt(color);
        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(flyingItemEntity)) {
            ServerPlayNetworking.send(serverPlayerEntity, Main.FLYING_STACK_LANDED, byteBuf);
        }
    }
}
