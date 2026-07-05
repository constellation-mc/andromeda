package dev.zenfyr.andromeda.modules.blocks.bed.power.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.blocks.bed.power.Power;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {

  public BedBlockMixin(Properties settings) {
    super(settings);
  }

  @ModifyExpressionValue(
      at = @At(value = "CONSTANT", args = "floatValue=5.0F"),
      method = "useWithoutItem")
  public float andromeda$explosionRedirect(
      float power, @Local(argsOnly = true, name = "level") Level level) {
    if (level.isClientSide()) return power;

    var config = level.am$get(Power.CONFIG);
    return config.available ? (float) config.power : power;
  }
}
