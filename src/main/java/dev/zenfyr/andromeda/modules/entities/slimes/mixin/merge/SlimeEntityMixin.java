package dev.zenfyr.andromeda.modules.entities.slimes.mixin.merge;

import dev.zenfyr.andromeda.modules.entities.slimes.Slimes;
import dev.zenfyr.pulsar.nbt.NbtUtil;
import dev.zenfyr.pulsar.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
abstract class SlimeEntityMixin extends Mob {

  @Shadow
  public abstract int getSize();

  @Shadow
  public abstract void setSize(int size, boolean heal);

  @Unique private int andromeda$mergeCD = MathUtil.nextInt(700, 2000);

  protected SlimeEntityMixin(EntityType<? extends Mob> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "registerGoals")
  private void andromeda$newGoal(CallbackInfo ci) {
    var config = this.level.am$get(Slimes.CONFIG);
    this.targetSelector.addGoal(
        2,
        new NearestAttackableTargetGoal<>(
            (Slime) (Object) this, Slime.class, 5, true, false, livingEntity -> {
              if (!config.available) return false;
              if (!config.merge) return false;
              if (this.andromeda$mergeCD > 0) return false;
              float d = livingEntity.distanceTo(this);
              return d <= 6
                  && (getSize() <= config.maxMerge && ((Slime) livingEntity).getSize() < getSize());
            }));
  }

  @Inject(at = @At("TAIL"), method = "push")
  private void andromeda$push(Entity entity, CallbackInfo ci) {
    var config = this.level.am$get(Slimes.CONFIG);
    if (!config.available) return;

    if (!config.merge) return;

    if (getTarget() instanceof Slime slime && slime == entity && this.andromeda$mergeCD == 0) {
      int size = (int) Math.round(slime.getSize() * 0.75 + getSize() * 0.75);

      slime.discard();
      this.setSize(size, true);
      this.andromeda$mergeCD = MathUtil.nextInt(700, 2000);
    }
  }

  @Inject(at = @At("TAIL"), method = "tick")
  private void andromeda$tick(CallbackInfo ci) {
    if (this.andromeda$mergeCD > 0) --this.andromeda$mergeCD;
  }

  @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
  private void andromeda$writeNbt(CompoundTag nbt, CallbackInfo ci) {
    nbt.putInt("AM-MergeCD", Math.max(this.andromeda$mergeCD, 0));
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(CompoundTag nbt, CallbackInfo ci) {
    this.andromeda$mergeCD = NbtUtil.getInt(nbt, "AM-MergeCD", MathUtil.nextInt(700, 2000));
  }
}
