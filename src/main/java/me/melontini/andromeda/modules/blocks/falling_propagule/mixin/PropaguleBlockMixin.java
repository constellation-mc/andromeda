package me.melontini.andromeda.modules.blocks.falling_propagule.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PropaguleBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PropaguleBlock.class)
abstract class PropaguleBlockMixin {

    @Shadow private static boolean isFullyGrown(BlockState state) {
        return false;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/PropaguleBlock;isFullyGrown(Lnet/minecraft/block/BlockState;)Z", shift = At.Shift.BEFORE), method = "randomTick")
    private void andromeda$randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (isFullyGrown(state) && random.nextInt(40) == 0) {
            FallingBlockEntity fallingBlock = new FallingBlockEntity(
                    world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    state.contains(Properties.WATERLOGGED) ? state.with(Properties.WATERLOGGED, Boolean.FALSE) : state);
            world.setBlockState(pos, state.getFluidState().getBlockState(), Block.NOTIFY_ALL);
            world.spawnEntity(fallingBlock);
        }
    }
}
