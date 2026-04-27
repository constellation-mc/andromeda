package dev.zenfyr.andromeda.modules.world.falling_beenests.mixin;

import dev.zenfyr.andromeda.modules.world.falling_beenests.BeeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeehiveBlock.class)
abstract class BeehiveBlockMixin {

  @Inject(at = @At("HEAD"), method = "updateShape", cancellable = true)
  private void andromeda$checkSupport(
      BlockState state,
      LevelReader world,
      ScheduledTickAccess scheduledTickAccess,
      BlockPos pos,
      Direction direction,
      BlockPos blockPos2,
      BlockState blockState2,
      RandomSource randomSource,
      CallbackInfoReturnable<BlockState> cir) {
    if (!(world instanceof Level)) return;
    for (Direction value : Direction.values()) {
      if (!world.getBlockState(pos.relative(value)).isAir()) {
        return;
      }
    }
    BeeUtil.trySpawnFallingBeeNest(
        (Level) world, pos, state, (BeehiveBlockEntity) world.getBlockEntity(pos));
    cir.setReturnValue(state.getFluidState().createLegacyBlock());
  }
}
