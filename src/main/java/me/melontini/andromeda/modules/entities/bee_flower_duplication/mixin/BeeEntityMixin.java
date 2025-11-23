package me.melontini.andromeda.modules.entities.bee_flower_duplication.mixin;

import me.melontini.andromeda.modules.entities.bee_flower_duplication.BeeFlowerDuplication;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
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
  private void andromeda$writeNbt(CompoundTag nbt, CallbackInfo ci) {
    if (this.andromeda$plantingCoolDown != 0)
      nbt.putInt("AM-plantingCoolDown", this.andromeda$plantingCoolDown);
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(CompoundTag nbt, CallbackInfo ci) {
    if (nbt.contains("AM-plantingCoolDown"))
      this.andromeda$plantingCoolDown = nbt.getInt("AM-plantingCoolDown");
  }

  @Unique private void andromeda$growFlower() {
    if (this.savedFlowerPos != null) {
      BlockState flowerState = level.getBlockState(savedFlowerPos);
      var config = level.am$get(BeeFlowerDuplication.CONFIG);
      if (!config.active) return;

      if (flowerState.getBlock() instanceof FlowerBlock flowerBlock) {
        andromeda$plantingCoolDown = level.random.nextIntBetweenInclusive(3600, 6490);
        for (int i = -2; i <= 2; i++) {
          for (int b = -2; b <= 2; b++) {
            for (int c = -2; c <= 2; c++) {
              BlockPos pos = new BlockPos(
                  savedFlowerPos.getX() + i, savedFlowerPos.getY() + b, savedFlowerPos.getZ() + c);
              if (level.getBlockState(pos).getBlock() instanceof AirBlock
                  && flowerBlock.canSurvive(flowerState, level, pos)) {
                if (level.random.nextInt(12) == 0) {
                  level.setBlockAndUpdate(pos, flowerState);
                }
              }
            }
          }
        }
      } else if (flowerState.getBlock() instanceof TallFlowerBlock flowerBlock
          && config.tallFlowers) {
        andromeda$plantingCoolDown = level.random.nextIntBetweenInclusive(3600, 8000);
        for (int i = -1; i <= 1; i++) {
          for (int b = -2; b <= 2; b++) {
            for (int c = -1; c <= 1; c++) {
              BlockPos pos = new BlockPos(
                  savedFlowerPos.getX() + i, savedFlowerPos.getY() + b, savedFlowerPos.getZ() + c);
              if (level.getBlockState(pos).getBlock() instanceof AirBlock
                  && flowerBlock.canSurvive(flowerState, level, pos)) {
                if (level.random.nextInt(6) == 0) {
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
