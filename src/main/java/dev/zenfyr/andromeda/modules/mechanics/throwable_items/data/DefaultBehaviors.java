package dev.zenfyr.andromeda.modules.mechanics.throwable_items.data;

import static dev.zenfyr.andromeda.modules.mechanics.throwable_items.Main.BRICKED;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class DefaultBehaviors {

  public static void init() {
    ItemBehaviorManager.register(
        (stack, fie, world, user, hitResult) -> {
          if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult result = (BlockHitResult) hitResult;

            Items.BONE_MEAL.useOn(new UseOnContext(
                world,
                user instanceof Player player ? player : null,
                InteractionHand.MAIN_HAND,
                stack,
                result));
          }
        },
        Items.BONE_MEAL);

    ItemBehaviorManager.register(
        (stack, fie, world, user, hitResult) -> {
          if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();
            entity.hurt(world.damageSources().source(BRICKED, user), 2);
            if (entity instanceof LivingEntity livingEntity) {
              livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
            if (entity instanceof NeutralMob angerable
                && user instanceof LivingEntity livingEntity) {
              angerable.setTarget(livingEntity);
            }
          }
          world.playSeededSound(
              null,
              fie.getX(),
              fie.getY(),
              fie.getZ(),
              SoundEvents.STONE_FALL,
              SoundSource.AMBIENT,
              (float) (fie.getDeltaMovement().normalize().length() * 1.5),
              1,
              world.getRandom().nextLong());
          world.addFreshEntity(new ItemEntity(world, fie.getX(), fie.getY(), fie.getZ(), stack));
        },
        Items.BRICK,
        Items.NETHER_BRICK);

    ItemBehaviorManager.register(
        (stack, fie, world, user, hitResult) -> {
          if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult result = (BlockHitResult) hitResult;
            BlockPos blockPos = result.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (blockState.getBlock() instanceof TntBlock) {
              TntBlock.explode(world, blockPos);
              world.removeBlock(blockPos, false);
              world.gameEvent(user, GameEvent.BLOCK_ACTIVATE, blockPos);
            } else {
              if (world
                  .getBlockState(blockPos = blockPos.relative(result.getDirection()))
                  .isAir()) {
                world.setBlockAndUpdate(blockPos, BaseFireBlock.getState(world, blockPos));
                world.gameEvent(user, GameEvent.BLOCK_PLACE, blockPos);
              }
            }

            RandomSource random = world.getRandom();
            world.playSound(
                null,
                fie.blockPosition(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS,
                1.0F,
                (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
          } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult result = (EntityHitResult) hitResult;
            Entity entity = result.getEntity();
            if (entity instanceof LivingEntity livingEntity)
              livingEntity.knockback(
                  0.4, -fie.getDeltaMovement().x(), -fie.getDeltaMovement().z());
            entity.setSecondsOnFire(8);
            RandomSource random = world.getRandom();
            world.playSound(
                null,
                fie.blockPosition(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS,
                1.0F,
                (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
          }
        },
        Items.FIRE_CHARGE);
  }
}
