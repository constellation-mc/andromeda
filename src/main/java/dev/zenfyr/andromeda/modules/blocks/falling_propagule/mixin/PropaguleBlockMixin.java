package dev.zenfyr.andromeda.modules.blocks.falling_propagule.mixin;

import dev.zenfyr.andromeda.modules.blocks.falling_propagule.FallingPropagule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MangrovePropaguleBlock.class)
abstract class PropaguleBlockMixin {

  @Shadow
  private static boolean isFullyGrown(BlockState state) {
    return false;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/block/MangrovePropaguleBlock;isFullyGrown(Lnet/minecraft/world/level/block/state/BlockState;)Z",
              shift = At.Shift.BEFORE),
      method = "randomTick")
  private void andromeda$randomTick(
      BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
    if (isFullyGrown(state)
        && random.nextInt(40) == 0
        && world.am$get(FallingPropagule.CONFIG).available) {
      FallingBlockEntity fallingBlock = new FallingBlockEntity(
          world,
          pos.getX() + 0.5,
          pos.getY(),
          pos.getZ() + 0.5,
          state.hasProperty(BlockStateProperties.WATERLOGGED)
              ? state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE)
              : state);
      world.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
      world.addFreshEntity(fallingBlock);
    }
  }
}
