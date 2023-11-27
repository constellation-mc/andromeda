package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import me.melontini.andromeda.modules.mechanics.throwable_items.Content;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.dark_matter.api.base.util.ColorUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ItemBehaviorAdder {

    public static ItemBehavior dataPack(ItemBehaviorData data) {
        return (stack, flyingItemEntity, world, user, hitResult) -> {//default behavior to handle datapacks
            if (!world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;

                switch (hitResult.getType()) {
                    case ENTITY -> executeCommands(serverWorld, flyingItemEntity, user, hitResult, data.on_entity_hit);
                    case BLOCK -> executeCommands(serverWorld, flyingItemEntity, user, hitResult, data.on_block_hit);
                    case MISS -> executeCommands(serverWorld, flyingItemEntity, user, hitResult, data.on_miss);
                }
                executeCommands(serverWorld, flyingItemEntity, user, hitResult, data.on_any_hit);

                sendParticlePacket(flyingItemEntity, flyingItemEntity.getPos(), data.spawn_item_particles, stack, data.spawn_colored_particles, ColorUtil.toColor(data.particle_colors.red(), data.particle_colors.green(), data.particle_colors.blue()));
            }
        };
    }

    private static void executeCommands(ServerWorld serverWorld, FlyingItemEntity flyingItemEntity, Entity user, HitResult hitResult, ItemBehaviorData.CommandHolder data) {
        if (data.item_commands() != null) {
            ServerCommandSource source = new ServerCommandSource(
                    serverWorld.getServer(), flyingItemEntity.getPos(), new Vec2f(flyingItemEntity.getPitch(), flyingItemEntity.getYaw()), serverWorld, 4, "AndromedaFlyingItem", TextUtil.literal("AndromedaFlyingItem"), serverWorld.getServer(), flyingItemEntity).withSilent();
            for (String command : data.item_commands()) {
                serverWorld.getServer().getCommandManager().executeWithPrefix(source, command);
            }
        }

        if (data.user_commands() != null && user != null) {
            ServerCommandSource source = new ServerCommandSource(
                    serverWorld.getServer(), user.getPos(), new Vec2f(user.getPitch(), user.getYaw()), serverWorld, 4, user.getEntityName(), TextUtil.literal(user.getEntityName()), serverWorld.getServer(), user).withSilent();
            for (String command : data.user_commands()) {
                serverWorld.getServer().getCommandManager().executeWithPrefix(source, command);
            }
        }

        if (data.server_commands() != null) {
            for (String command : data.server_commands()) {
                serverWorld.getServer().getCommandManager().executeWithPrefix(serverWorld.getServer().getCommandSource().withSilent(), command);
            }
        }

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity) {
                if (data.hit_entity_commands() != null) {
                    ServerCommandSource source = new ServerCommandSource(
                            serverWorld.getServer(), entity.getPos(), new Vec2f(entity.getPitch(), entity.getYaw()), serverWorld, 4, entity.getEntityName(), TextUtil.literal(entity.getEntityName()), serverWorld.getServer(), entity).withSilent();
                    for (String command : data.hit_entity_commands()) {
                        serverWorld.getServer().getCommandManager().executeWithPrefix(source, command);
                    }
                }
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            if (data.hit_block_commands() != null) {
                Vec3d vec3d = new Vec3d(blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ());
                ServerCommandSource source = new ServerCommandSource(
                        serverWorld.getServer(), vec3d, new Vec2f(0, 0), serverWorld, 4, "AndromedaFlyingItem", TextUtil.literal("AndromedaFlyingItem"), serverWorld.getServer(), flyingItemEntity).withSilent();
                for (String command : data.hit_block_commands()) {
                    serverWorld.getServer().getCommandManager().executeWithPrefix(source, command);
                }
            }
        }
    }

    public static void sendParticlePacketInt(FlyingItemEntity flyingItemEntity, Vec3d pos, boolean item, ItemStack stack, boolean colored, int red, int green, int blue) {
        sendParticlePacket(flyingItemEntity, pos, item, stack, colored, ColorUtil.toColor(red, green, blue));
    }

    public static void sendParticlePacketInt(FlyingItemEntity flyingItemEntity, Vec3d pos, ItemStack stack, boolean colored, int red, int green, int blue) {
        sendParticlePacket(flyingItemEntity, pos, true, stack, colored, ColorUtil.toColor(red, green, blue));
    }

    public static void sendParticlePacket(FlyingItemEntity flyingItemEntity, Vec3d pos, boolean item, ItemStack stack, boolean colored, int color) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeDouble(pos.getX());
        byteBuf.writeDouble(pos.getY());
        byteBuf.writeDouble(pos.getZ());
        byteBuf.writeBoolean(item);
        byteBuf.writeItemStack(stack);
        byteBuf.writeBoolean(colored);
        byteBuf.writeVarInt(color);
        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(flyingItemEntity)) {
            ServerPlayNetworking.send(serverPlayerEntity, Content.FLYING_STACK_LANDED, byteBuf);
        }
    }

    public static void sendParticlePacket(FlyingItemEntity flyingItemEntity, Vec3d pos, ItemStack stack, boolean colored, int color) {
        sendParticlePacket(flyingItemEntity, pos, true, stack, colored, color);
    }

    public static void addBehavior(Item item, ItemBehavior behavior) {
        ItemBehaviorManager.addBehavior(item, behavior);
    }

    public static void addBehavior(ItemBehavior behavior, Item... items) {
        ItemBehaviorManager.addBehaviors(behavior, items);
    }

}
