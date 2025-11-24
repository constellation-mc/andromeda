package me.melontini.andromeda.modules.blocks.cactus_bottle_filling.mixin;

import me.melontini.andromeda.modules.blocks.cactus_bottle_filling.Main;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CactusBlock.class)
abstract class CactusBlockMixin {

  @Inject(at = @At("TAIL"), method = "createBlockStateDefinition")
  private void andromeda$appendProperties(
      StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
    builder.add(Main.WATER_LEVEL_3);
  }
}
