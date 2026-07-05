package dev.zenfyr.andromeda.modules.blocks.cactus_bottle_filling.mixin;

import dev.zenfyr.andromeda.modules.blocks.cactus_bottle_filling.CactusFiller;
import dev.zenfyr.andromeda.modules.blocks.cactus_bottle_filling.CactusFillerMain;
import dev.zenfyr.pulsar.api.itemstack.ItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
abstract class BlockBehaviourMixin {

  @Inject(at = @At("HEAD"), method = "useItemOn", cancellable = true)
  private void andromeda$onUse(
      ItemStack itemStack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hitResult,
      CallbackInfoReturnable<InteractionResult> cir) {
    if (state.getBlock() instanceof CactusBlock) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.is(Items.GLASS_BOTTLE)) {
        BlockPos pos1 = pos;
        while (true) {
          BlockState state1 = level.getBlockState(pos1 = pos1.above());
          if (state.is(state1.getBlock())) {
            state = state1;
          } else {
            break;
          }
        }

        if (!level.isClientSide() && level.am$get(CactusFiller.CONFIG).available) {
          player.setItemInHand(
              hand,
              ItemUtils.createFilledResult(
                  stack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
          player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

          if (state.getValue(CactusFillerMain.WATER_LEVEL_3) == 3) {
            level.destroyBlock(pos1.below(), false, player);
            ItemStackUtil.spawnVelocity(
                pos1, Items.DEAD_BUSH.getDefaultInstance(), level, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2);
          } else {
            level.setBlockAndUpdate(pos1.below(), state.cycle(CactusFillerMain.WATER_LEVEL_3));
          }

          ((ServerLevel) level)
              .sendParticles(
                  ParticleTypes.FALLING_WATER,
                  pos.getX() + 0.5,
                  pos.getY() + 0.5,
                  pos.getZ() + 0.5,
                  5,
                  0.6,
                  0.5,
                  0.6,
                  0.5);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
      }
    }
  }
}
