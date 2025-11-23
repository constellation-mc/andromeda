package me.melontini.andromeda.modules.blocks.better_fletching_table.mixin;

import me.melontini.andromeda.modules.blocks.better_fletching_table.FletchingScreenHandler;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.FletchingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FletchingTableBlock.class)
abstract class FletchingTableBlockMixin extends CraftingTableBlock {

  public FletchingTableBlockMixin(Properties settings) {
    super(settings);
  }

  @Inject(at = @At("HEAD"), method = "use", cancellable = true)
  private void andromeda$onUse(
      BlockState state,
      Level world,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit,
      CallbackInfoReturnable<InteractionResult> cir) {
    if (state.is(Blocks.FLETCHING_TABLE)) {
      if (player.level.isClientSide) {
        cir.setReturnValue(InteractionResult.SUCCESS);
        return;
      }

      player.openMenu(new SimpleMenuProvider(
          (syncId, inv, player1) ->
              new FletchingScreenHandler(syncId, inv, ContainerLevelAccess.create(world, pos)),
          TextUtil.translatable("block.minecraft.fletching_table")));
      cir.setReturnValue(InteractionResult.SUCCESS);
    }
  }
}
