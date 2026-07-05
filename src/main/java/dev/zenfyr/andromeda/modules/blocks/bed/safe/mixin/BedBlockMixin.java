package dev.zenfyr.andromeda.modules.blocks.bed.safe.mixin;

import dev.zenfyr.andromeda.modules.blocks.bed.safe.Safe;
import dev.zenfyr.pulsar.api.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
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
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      BlockHitResult hitResult,
      CallbackInfoReturnable<InteractionResult> cir) {
    if (level.isClientSide()) return;

    if (level
        .environmentAttributes()
        .getValue(EnvironmentAttributes.BED_RULE, pos)
        .explodes()) {
      if (level.am$get(Safe.CONFIG).available) {
        player.sendOverlayMessage(TextUtil.translatable("action.andromeda.safebeds"));
        cir.setReturnValue(InteractionResult.SUCCESS);
      }
    }
  }
}
