package dev.zenfyr.andromeda.modules.world.crop_temperature.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.zenfyr.andromeda.modules.world.crop_temperature.PlantTemperatureData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
abstract class ServerLevelMixin {

  @WrapWithCondition(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"),
      method = "tickChunk")
  private boolean andromeda$tickPlants(
      BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    return PlantTemperatureData.roll(
        pos, state, level.getBiome(pos).value().getBaseTemperature(), level);
  }
}
