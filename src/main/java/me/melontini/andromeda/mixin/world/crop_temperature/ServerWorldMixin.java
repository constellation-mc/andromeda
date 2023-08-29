package me.melontini.andromeda.mixin.world.crop_temperature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.andromeda.util.data.PlantTemperatureData;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWorld.class)
@MixinRelatedConfigOption("temperatureBasedCropGrowthSpeed")
public class ServerWorldMixin {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"), method = "tickChunk")
    private void andromeda$tickPlants(BlockState state, ServerWorld world, BlockPos pos, Random random, Operation<Void> operation) {
        if (Andromeda.CONFIG.temperatureBasedCropGrowthSpeed) {
            if (state.getBlock() instanceof PlantBlock) {
                PlantTemperatureData data = Andromeda.get().PLANT_DATA.get(state.getBlock());
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
