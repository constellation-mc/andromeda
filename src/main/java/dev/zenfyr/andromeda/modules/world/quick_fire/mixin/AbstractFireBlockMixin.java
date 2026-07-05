package dev.zenfyr.andromeda.modules.world.quick_fire.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.world.quick_fire.QuickFire;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
abstract class AbstractFireBlockMixin extends BaseFireBlock {

  @Shadow
  protected abstract void checkBurnOut(
      Level level, BlockPos pos, int chance, RandomSource random, int age);

  @Unique private static final ThreadLocal<Boolean> LOCAL = ThreadLocal.withInitial(() -> Boolean.FALSE);

  public AbstractFireBlockMixin(Properties settings, float damage) {
    super(settings, damage);
  }

  @ModifyVariable(method = "checkBurnOut", at = @At("LOAD"), argsOnly = true, name = "chance")
  public int andromeda$spreadFire0(int chance) {
    return Boolean.TRUE.equals(LOCAL.get()) ? (int) (chance * 0.8) : chance;
  }

  @ModifyExpressionValue(
      method = "checkBurnOut",
      at = @At(value = "CONSTANT", args = "intValue=10"))
  public int andromeda$spreadFire01(int value) {
    return Boolean.TRUE.equals(LOCAL.get()) ? (int) Math.ceil(value / 3d) : value;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/block/FireBlock;checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V",
              ordinal = 0,
              shift = At.Shift.BEFORE),
      method = "tick")
  public void andromeda$trySpreadBlocks(
      BlockState state,
      ServerLevel level,
      BlockPos pos,
      RandomSource random,
      CallbackInfo ci,
      @Local(name = "age") int age,
      @Local(name = "extra") int extra) {
    if (level.am$get(QuickFire.CONFIG).available) {
      try {
        LOCAL.set(Boolean.TRUE);
        for (int x = -3; x < 3; x++) {
          for (int y = -3; y < 3; y++) {
            for (int z = -3; z < 3; z++) {
              this.checkBurnOut(
                  level,
                  new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z),
                  300 + extra,
                  random,
                  age);
            }
          }
        }
      } finally {
        LOCAL.remove();
      }
    }
  }
}
