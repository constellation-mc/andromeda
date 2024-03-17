package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.Event;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.EventType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;

import java.util.Set;

import static me.melontini.andromeda.modules.mechanics.throwable_items.Main.BRICKED;

public class DefaultBehaviors {

    private static final Set<Item> DYE_ITEMS = Set.of(
            Items.RED_DYE, Items.BLUE_DYE, Items.LIGHT_BLUE_DYE,
            Items.CYAN_DYE, Items.BLACK_DYE, Items.BROWN_DYE,
            Items.GREEN_DYE, Items.PINK_DYE, Items.PURPLE_DYE,
            Items.YELLOW_DYE, Items.WHITE_DYE, Items.ORANGE_DYE,
            Items.LIME_DYE, Items.MAGENTA_DYE, Items.LIGHT_GRAY_DYE,
            Items.GRAY_DYE);

    public static void init() {
        ItemBehaviorManager.register(Event.of(EventType.ENTITY, context -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeItemStack(context.stack());

            EntityHitResult entityHitResult = (EntityHitResult) context.hitResult();
            if (entityHitResult.getEntity() instanceof PlayerEntity player) {
                ServerPlayNetworking.send((ServerPlayerEntity) player, Main.COLORED_FLYING_STACK_LANDED, buf);
            }
        }), DYE_ITEMS);

        ItemBehaviorManager.register(Event.of(EventType.ENTITY, context -> {
            var world = context.world(); var user = context.user();

            Entity entity = ((EntityHitResult) context.hitResult()).getEntity();
            entity.damage(new DamageSource(MiscUtil.getTypeReference(world, BRICKED), user), 2);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
            }
            if (entity instanceof Angerable angerable && user instanceof LivingEntity livingEntity) {
                angerable.setTarget(livingEntity);
            }
        }), Items.BRICK, Items.NETHER_BRICK);

        ItemBehaviorManager.register(Event.of(EventType.ANY, context -> {
            var world = context.world(); var fie = context.fie();

            world.playSound(null, fie.getX(), fie.getY(), fie.getZ(), SoundEvents.BLOCK_STONE_FALL, SoundCategory.AMBIENT, (float) (fie.getVelocity().normalize().length() * 1.5), 1, world.getRandom().nextLong());
            world.spawnEntity(new ItemEntity(world, fie.getX(), fie.getY(), fie.getZ(), context.stack()));
        }), Items.BRICK, Items.NETHER_BRICK);

        ItemBehaviorManager.register(Event.of(EventType.BLOCK, context -> {
            var world = context.world(); var fie = context.fie();
            BlockHitResult result = (BlockHitResult) context.hitResult();
            BlockPos blockPos = result.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (blockState.getBlock() instanceof TntBlock) {
                TntBlock.primeTnt(world, blockPos);
                world.removeBlock(blockPos, false);
                world.emitGameEvent(context.user(), GameEvent.BLOCK_ACTIVATE, blockPos);
            } else {
                if (world.getBlockState(blockPos = blockPos.offset(result.getSide())).isAir()) {
                    world.setBlockState(blockPos, AbstractFireBlock.getState(world, blockPos));
                    world.emitGameEvent(context.user(), GameEvent.BLOCK_PLACE, blockPos);
                }
            }

            Random random = world.getRandom();
            world.playSound(null, fie.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
        }), Items.FIRE_CHARGE);

        ItemBehaviorManager.register(Event.of(EventType.BLOCK, context -> {
            var world = context.world(); var fie = context.fie();
            EntityHitResult result = (EntityHitResult) context.hitResult();
            Entity entity = result.getEntity();
            if (entity instanceof LivingEntity livingEntity)
                livingEntity.takeKnockback(0.4, -fie.getVelocity().getX(), -fie.getVelocity().getZ());
            entity.setOnFireFor(8);
            Random random = world.getRandom();
            world.playSound(null, fie.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
        }), Items.FIRE_CHARGE);
    }
}
