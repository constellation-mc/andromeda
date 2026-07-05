package dev.zenfyr.andromeda.modules.world.crop_temperature.mixin;

import dev.zenfyr.andromeda.modules.world.crop_temperature.PlantTemperature;
import dev.zenfyr.andromeda.modules.world.crop_temperature.PlantTemperatureData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
abstract class BoneMealItemMixin {

  @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
  private void andromeda$useOnFertilizable(
      UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    if (level.isClientSide()) return;

    BlockState state = level.getBlockState(pos);
    if (level.am$get(PlantTemperature.CONFIG).affectBoneMeal) {
      if (!PlantTemperatureData.roll(
          pos, state, level.getBiome(pos).value().getBaseTemperature(), (ServerLevel) level)) {
        cir.setReturnValue(InteractionResult.FAIL);
      }
    }
  }
}
