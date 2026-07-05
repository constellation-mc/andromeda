package dev.zenfyr.andromeda.modules.world.moist_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.zenfyr.andromeda.modules.world.moist_control.MoistControl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
abstract class FarmlandBlockMixin {

  @Inject(at = @At("HEAD"), method = "isNearWater")
  private static void andromeda$prepareRule(
      LevelReader level,
      BlockPos pos,
      CallbackInfoReturnable<Boolean> cir,
      @Share("value") LocalIntRef ref) {
    if (level instanceof ServerLevel sw) {
      ref.set(sw.am$get(MoistControl.CONFIG).customMoisture);
    }
  }

  @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "intValue=4"), method = "isNearWater")
  private static int andromeda$modifyMoisture(int original, @Share("value") LocalIntRef ref) {
    return ref.get();
  }

  @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "intValue=-4"), method = "isNearWater")
  private static int andromeda$modifyMoistureNegative(
      int original, @Share("value") LocalIntRef ref) {
    return -ref.get();
  }
}
