package me.melontini.andromeda.modules.world.falling_beenests.mixin;

import me.melontini.andromeda.modules.world.falling_beenests.BeeUtil;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeehiveBlock.class)
abstract class BeehiveBlockMixin {

  @Inject(at = @At("HEAD"), method = "updateShape", cancellable = true)
  private void andromeda$checkSupport(
          BlockState state,
          Direction direction,
          BlockState neighborState,
          LevelAccessor world,
          BlockPos pos,
          BlockPos neighborPos,
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
