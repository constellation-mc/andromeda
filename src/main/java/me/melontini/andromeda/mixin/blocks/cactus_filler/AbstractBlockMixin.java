package me.melontini.andromeda.mixin.blocks.cactus_filler;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.BlockUtil;
import me.melontini.andromeda.util.ItemStackUtil;
import me.melontini.andromeda.util.MiscUtil;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
@MixinRelatedConfigOption("cactusBottleFilling")
public class AbstractBlockMixin {
    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
    private void andromeda$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (Andromeda.CONFIG.cactusBottleFilling) if (state.getBlock() instanceof CactusBlock) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(Items.GLASS_BOTTLE)) {
                BlockPos pos1 = pos;
                while (true) {
                    BlockState state1 = world.getBlockState(pos1 = pos1.up());
                    if (state1.getBlock() instanceof CactusBlock) {
                        state = state1;
                    } else {
                        break;
                    }
                }

                AndromedaLog.devInfo(state + " [" + MiscUtil.blockPosAsString(pos1.down()) + "]");

                if (!world.isClient()) {
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
                    player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

                    if (state.get(BlockUtil.WATER_LEVEL_3) == 3) {
                        world.breakBlock(pos1.down(), false, player);
                        ItemStackUtil.spawnWithRVelocity(pos1, Items.DEAD_BUSH.getDefaultStack(), world, 0.2);
                    } else {
                        world.setBlockState(pos1.down(), state.cycle(BlockUtil.WATER_LEVEL_3));
                    }

                    ((ServerWorld) world).spawnParticles(ParticleTypes.FALLING_WATER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.6, 0.5, 0.6, 0.5);
                }

                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }
}
