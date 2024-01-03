package me.melontini.andromeda.modules.world.moist_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.melontini.andromeda.modules.world.moist_control.MoistControl;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
abstract class FarmlandMixin {

    @Inject(at = @At("HEAD"), method = "isWaterNearby")
    private static void andromeda$prepareRule(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share("value") LocalIntRef ref) {
        if (world instanceof ServerWorld sw) {
            ref.set(sw.am$get(MoistControl.class).customMoisture);
        }
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "intValue=4"), method = "isWaterNearby")
    private static int andromeda$modifyMoisture(int original, @Share("value") LocalIntRef ref) {
        return ref.get();
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "intValue=-4"), method = "isWaterNearby")
    private static int andromeda$modifyMoistureNegative(int original, @Share("value") LocalIntRef ref) {
        return -ref.get();
    }
}
