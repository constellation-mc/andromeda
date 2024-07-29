package me.melontini.andromeda.modules.blocks.bed.unsafe.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.blocks.bed.unsafe.Unsafe;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin {

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"),
      method = "onUse")
  private boolean andromeda$explode(
      boolean original,
      @Local(argsOnly = true) World world,
      @Local(argsOnly = true) BlockPos pos,
      @Local(argsOnly = true) BlockState state,
      @Local(argsOnly = true) PlayerEntity player,
      @Local(argsOnly = true) Hand hand) {
    if (world.isClient()) return original;

    return !world
            .am$get(Unsafe.CONFIG)
            .available
            .asBoolean(LootContextUtil.block(
                world, Vec3d.ofCenter(pos), state, player.getStackInHand(hand), player))
        && original;
  }
}
