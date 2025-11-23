package me.melontini.andromeda.modules.blocks.bed.safe.mixin;

import static net.minecraft.world.level.block.BedBlock.canSetSpawn;

import me.melontini.andromeda.modules.blocks.bed.safe.Safe;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {

  public BedBlockMixin(Properties settings) {
    super(settings);
  }

  @Inject(at = @At("HEAD"), method = "use", cancellable = true)
  public void andromeda$onUse(
      BlockState state,
      @NotNull Level world,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit,
      CallbackInfoReturnable<InteractionResult> cir) {
    if (world.isClientSide()) return;

    if (!canSetSpawn(world)) {
      if (world.am$get(Safe.CONFIG).active) {
        player.displayClientMessage(TextUtil.translatable("action.andromeda.safebeds"), true);
        cir.setReturnValue(InteractionResult.SUCCESS);
      }
    }
  }
}
