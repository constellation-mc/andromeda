package dev.zenfyr.andromeda.modules.entities.ghast_tweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.entities.ghast_tweaks.GhastExplosionDuck;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
abstract class ExplosionMixin implements GhastExplosionDuck {

  @Shadow
  @Final
  private Level level;

  @Unique private final ObjectArrayList<BlockPos> affectedObsidian = new ObjectArrayList<>();

  @Unique private boolean affectObsidian = false;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"),
      method = "explode")
  private void collectAffectedObsidian(
      CallbackInfo ci,
      @Local(index = 14) float h,
      @Local(index = 22) BlockPos pos,
      @Local(index = 23) BlockState state) {
    if (!affectObsidian || state.getBlock() != Blocks.OBSIDIAN) return;
    if (h - 0.64 > 0 && level.random.nextFloat() >= 0.2f) affectedObsidian.add(pos);
  }

  @Inject(
      at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Explosion;fire:Z"),
      method = "finalizeExplosion")
  private void affectObsidian(boolean particles, CallbackInfo ci) {
    for (BlockPos blockPos : affectedObsidian) {
      level.setBlockAndUpdate(blockPos, Blocks.CRYING_OBSIDIAN.defaultBlockState());
    }
  }

  @Override
  public void andromeda$convertObsidian(boolean b) {
    this.affectObsidian = b;
  }
}
