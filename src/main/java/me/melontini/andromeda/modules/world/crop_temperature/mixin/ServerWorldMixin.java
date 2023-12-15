package me.melontini.andromeda.modules.world.crop_temperature.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperatureData;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(ServerWorld.class)
class ServerWorldMixin {

    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"), method = "tickChunk")
    private boolean andromeda$tickPlants(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            if (state.getBlock() instanceof PlantBlock) {
                PlantTemperatureData data = PlantTemperatureData.PLANT_DATA.get(state.getBlock());
                if (data != null) {
                    float temp = ((ServerWorld) (Object) this).getBiome(pos).value().getTemperature();
                    if ((temp > data.max() && temp <= data.aMax()) || (temp < data.min() && temp >= data.aMin())) {
                        return MathStuff.nextInt(0, 1) != 0;
                    } else
                        return (!(temp > data.aMax())) && (!(temp < data.aMin()));
                }
            }
        return true;
    }
}
