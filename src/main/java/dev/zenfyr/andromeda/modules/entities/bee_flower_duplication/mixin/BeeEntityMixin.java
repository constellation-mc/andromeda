package dev.zenfyr.andromeda.modules.entities.bee_flower_duplication.mixin;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.modules.entities.bee_flower_duplication.BeeFlowerDuplication;
import dev.zenfyr.andromeda.modules.misc.unknown.RoseOfTheValley;
import dev.zenfyr.andromeda.modules.misc.unknown.Unknown;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bee.class)
abstract class BeeEntityMixin extends Animal {

  @Shadow
  @Nullable BlockPos savedFlowerPos;

  @Shadow
  Bee.BeePollinateGoal beePollinateGoal;

  @Unique private int andromeda$plantingCoolDown;

  protected BeeEntityMixin(EntityType<? extends Animal> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/animal/Animal;tick()V",
              shift = At.Shift.AFTER),
      method = "tick")
  private void andromeda$tick(CallbackInfo ci) {
    if (this.andromeda$plantingCoolDown > 0) this.andromeda$plantingCoolDown--;

    if (this.beePollinateGoal != null) {
      if (this.beePollinateGoal.isPollinating()
          && this.beePollinateGoal.hasPollinatedLongEnough()
          && this.andromeda$canPlant()) {
        this.andromeda$growFlower();
      }
    }
  }

  @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
  private void andromeda$writeNbt(ValueOutput valueOutput, CallbackInfo ci) {
    if (this.andromeda$plantingCoolDown != 0)
      valueOutput.putInt("AM-plantingCoolDown", this.andromeda$plantingCoolDown);
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(ValueInput valueInput, CallbackInfo ci) {
    if (valueInput.contains("AM-plantingCoolDown"))
      this.andromeda$plantingCoolDown = valueInput.getIntOr("AM-plantingCoolDown", 0);
  }

  @Unique private void andromeda$growFlower() {
    if (this.savedFlowerPos != null) {
      BlockState flowerState = level.getBlockState(savedFlowerPos);
      var config = level.am$get(BeeFlowerDuplication.CONFIG);
      if (!config.available) return;

      if (flowerState.getBlock() instanceof FlowerBlock flowerBlock) {
        andromeda$plantingCoolDown = level.getRandom().nextIntBetweenInclusive(3600, 6490);
        for (int i = -2; i <= 2; i++) {
          for (int b = -2; b <= 2; b++) {
            for (int c = -2; c <= 2; c++) {
              BlockPos pos = new BlockPos(
                  savedFlowerPos.getX() + i, savedFlowerPos.getY() + b, savedFlowerPos.getZ() + c);
              if (level.getBlockState(pos).getBlock() instanceof AirBlock
                  && flowerBlock.canSurvive(flowerState, level, pos)) {
                if (level.getRandom().nextInt(12) == 0) {
                  if (ModuleManager.get().get(Unknown.class).isPresent()
                      && level.getRandom().nextInt(100) == 0) {
                    level.setBlockAndUpdate(
                        pos,
                        RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow().defaultBlockState());
                  } else {
                    level.setBlockAndUpdate(pos, flowerState);
                  }
                }
              }
            }
          }
        }
      } else if (flowerState.getBlock() instanceof TallFlowerBlock flowerBlock
          && config.tallFlowers) {
        andromeda$plantingCoolDown = level.getRandom().nextIntBetweenInclusive(3600, 8000);
        for (int i = -1; i <= 1; i++) {
          for (int b = -2; b <= 2; b++) {
            for (int c = -1; c <= 1; c++) {
              BlockPos pos = new BlockPos(
                  savedFlowerPos.getX() + i, savedFlowerPos.getY() + b, savedFlowerPos.getZ() + c);
              if (level.getBlockState(pos).getBlock() instanceof AirBlock
                  && flowerBlock.canSurvive(flowerState, level, pos)) {
                if (level.getRandom().nextInt(6) == 0) {
                  TallFlowerBlock.placeAt(level, flowerState, pos, Block.UPDATE_CLIENTS);
                }
              }
            }
          }
        }
      }
    }
  }

  @Unique private boolean andromeda$canPlant() {
    return this.andromeda$plantingCoolDown == 0;
  }
}
