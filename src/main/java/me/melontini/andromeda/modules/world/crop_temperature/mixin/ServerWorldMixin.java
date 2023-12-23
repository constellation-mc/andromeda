package me.melontini.andromeda.modules.world.crop_temperature.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperatureData;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(ServerWorld.class)
class ServerWorldMixin {

    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"), method = "tickChunk")
    private boolean andromeda$tickPlants(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        return PlantTemperatureData.roll(state.getBlock(), world.getBiome(pos).value().getTemperature(), world);
    }
}
