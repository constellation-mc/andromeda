package dev.zenfyr.andromeda.modules.world.falling_beenests.mixin;

import dev.zenfyr.andromeda.modules.world.falling_beenests.BeeUtil;
import dev.zenfyr.andromeda.modules.world.falling_beenests.CanBeeNestsFall;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveBlockEntity.class)
abstract class BeehiveBlockEntityMixin extends BlockEntity {

  @Unique private boolean andromeda$FromFallen;

  public BeehiveBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Inject(at = @At("HEAD"), method = "serverTick")
  private static void andromeda$fallingHive(
      @NotNull Level level,
      BlockPos blockPos,
      BlockState state,
      BeehiveBlockEntity entity,
      CallbackInfo ci) {
    if (state.getBlock() != Blocks.BEE_NEST) return;

    if (level.am$get(CanBeeNestsFall.CONFIG).available && level.getRandom().nextInt(32000) == 0) {
      if (!level.getBlockState(blockPos.relative(Direction.DOWN)).isAir()) return;

      BlockState up = level.getBlockState(blockPos.relative(Direction.UP));
      if (!up.is(BlockTags.LOGS) && !up.is(BlockTags.LEAVES)) return;

      for (Direction direction : BeeUtil.AROUND_BLOCK_DIRECTIONS) {
        if (level.getBlockState(blockPos.relative(direction)).is(BlockTags.LOGS)) {
          BeeUtil.trySpawnFallingBeeNest(level, blockPos, state, entity);
          break;
        }
      }
    }
  }

  @Inject(at = @At("TAIL"), method = "loadAdditional")
  private void andromeda$readNbt(ValueInput valueInput, CallbackInfo ci) {
    if (valueInput.contains("AM-FromFallenBlock"))
      this.andromeda$FromFallen = valueInput.getBooleanOr("AM-FromFallenBlock", false);
  }

  @Inject(at = @At("TAIL"), method = "saveAdditional")
  private void andromeda$writeNbt(ValueOutput valueOutput, CallbackInfo ci) {
    if (this.andromeda$FromFallen) valueOutput.putBoolean("AM-FromFallenBlock", true);
  }
}
