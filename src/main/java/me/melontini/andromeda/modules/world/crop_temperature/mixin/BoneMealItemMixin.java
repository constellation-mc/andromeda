package me.melontini.andromeda.modules.world.crop_temperature.mixin;

import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperature;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperatureData;
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
      UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
    Level world = ctx.getLevel();
    BlockPos pos = ctx.getClickedPos();
    if (world.isClientSide()) return;

    BlockState state = world.getBlockState(pos);
    if (world.am$get(PlantTemperature.CONFIG).affectBoneMeal) {
      if (!PlantTemperatureData.roll(
          pos, state, world.getBiome(pos).value().getBaseTemperature(), (ServerLevel) world)) {
        cir.setReturnValue(InteractionResult.FAIL);
      }
    }
  }
}
