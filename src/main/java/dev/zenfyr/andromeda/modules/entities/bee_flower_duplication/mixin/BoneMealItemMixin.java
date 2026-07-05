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
      ItemStack itemStack, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
    if (level.isClientSide()) return;

    BlockState blockState = level.getBlockState(pos);
    var config = level.am$get(BeeFlowerDuplication.CONFIG);
    if (!config.available || !config.tallFlowers) return;

    if (blockState.getBlock() instanceof TallFlowerBlock) {
      if (ModuleManager.get().get("misc.unknown").isPresent()
          && level.getRandom().nextInt(100) == 0) {
        level.explode(
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
