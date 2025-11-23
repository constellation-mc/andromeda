package me.melontini.andromeda.modules.blocks.bed.unsafe.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.blocks.bed.unsafe.Unsafe;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin {

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/level/block/BedBlock;canSetSpawn(Lnet/minecraft/world/level/Level;)Z"),
      method = "use")
  private boolean andromeda$explode(
      boolean original,
      @Local(argsOnly = true) Level world,
      @Local(argsOnly = true) BlockPos pos,
      @Local(argsOnly = true) BlockState state,
      @Local(argsOnly = true) Player player,
      @Local(argsOnly = true) InteractionHand hand) {
    if (world.isClientSide()) return original;

    return original
        && !world
            .am$get(Unsafe.CONFIG)
            .available
            .asBoolean(LootContextBuilder.block(
                world,
                builder -> builder.origin(pos).state(state).tool(player, hand).thisEntity(player)));
  }
}
