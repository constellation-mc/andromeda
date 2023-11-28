package me.melontini.andromeda.modules.world.crop_temperature.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperature;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperatureData;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(ServerWorld.class)
class ServerWorldMixin {
    @Unique
    private static final PlantTemperature am$tbpgs = ModuleManager.quick(PlantTemperature.class);

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"), method = "tickChunk")
    private void andromeda$tickPlants(BlockState state, ServerWorld world, BlockPos pos, Random random, Operation<Void> operation) {
        if (am$tbpgs.config().enabled) {
            if (state.getBlock() instanceof PlantBlock) {
                PlantTemperatureData data = PlantTemperatureData.PLANT_DATA.get(state.getBlock());
                if (data != null) {
                    float temp = ((ServerWorld) (Object) this).getBiome(pos).value().getTemperature();
                    if ((temp > data.max() && temp <= data.aMax()) || (temp < data.min() && temp >= data.aMin())) {
                        if (MathStuff.threadRandom().nextInt(2) == 0) {
                            return;
                        }
                    } else if ((temp > data.aMax()) || (temp < data.aMin())) {
                        return;
                    }
                }
            }
        }
        operation.call(state, world, pos, random);
    }
}
