package dev.zenfyr.andromeda.modules.blocks.bed.unsafe.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.blocks.bed.unsafe.Unsafe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin {

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/block/BedBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z"),
      method = "useWithoutItem")
  private boolean andromeda$explode(boolean original, @Local(argsOnly = true) Level world) {
    if (world.isClientSide()) return original;

    return original && !world.am$get(Unsafe.CONFIG).available;
  }
}
