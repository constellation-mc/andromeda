package dev.zenfyr.andromeda.modules.entities.bee_flower_duplication.mixin;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.modules.entities.bee_flower_duplication.BeeFlowerDuplication;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
abstract class BoneMealItemMixin {

  @Inject(at = @At("HEAD"), method = "growCrop", cancellable = true)
  private static void andromeda$useOnFertilizable(
      ItemStack stack, Level world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
    if (world.isClientSide()) return;

    BlockState blockState = world.getBlockState(pos);
    var config = world.am$get(BeeFlowerDuplication.CONFIG);
    if (!config.available || !config.tallFlowers) return;

    if (blockState.getBlock() instanceof TallFlowerBlock) {
      if (ModuleManager.get().get("misc.unknown").isPresent()
          && world.getRandom().nextInt(100) == 0) {
        world.explode(
            null,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            3.0F,
            false,
            Level.ExplosionInteraction.BLOCK);
      }
      cir.setReturnValue(false);
    }
  }
}
