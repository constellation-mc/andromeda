package dev.zenfyr.andromeda.modules.blocks.bed.safe.mixin;

import static net.minecraft.world.level.block.BedBlock.canSetSpawn;

import dev.zenfyr.andromeda.modules.blocks.bed.safe.Safe;
import dev.zenfyr.pulsar.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {

  public BedBlockMixin(Properties settings) {
    super(settings);
  }

  @Inject(at = @At("HEAD"), method = "useWithoutItem", cancellable = true)
  public void andromeda$onUse(
      BlockState blockState,
      Level world,
      BlockPos blockPos,
      Player player,
      BlockHitResult blockHitResult,
      CallbackInfoReturnable<InteractionResult> cir) {
    if (world.isClientSide()) return;

    if (!canSetSpawn(world)) {
      if (world.am$get(Safe.CONFIG).available) {
        player.displayClientMessage(TextUtil.translatable("action.andromeda.safebeds"), true);
        cir.setReturnValue(InteractionResult.SUCCESS);
      }
    }
  }
}
