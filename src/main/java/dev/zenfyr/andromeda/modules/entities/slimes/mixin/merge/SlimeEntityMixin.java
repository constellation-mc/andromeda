package dev.zenfyr.andromeda.modules.entities.slimes.mixin.merge;

import dev.zenfyr.andromeda.modules.entities.slimes.Slimes;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
  public abstract void setSize(int size, boolean updateHealth);

  @Unique private int andromeda$mergeCD = MathUtil.nextInt(700, 2000);

  protected SlimeEntityMixin(EntityType<? extends Mob> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(at = @At("TAIL"), method = "registerGoals")
  private void andromeda$newGoal(CallbackInfo ci) {
    this.targetSelector.addGoal(
        2,
        new NearestAttackableTargetGoal<>(
            (Slime) (Object) this, Slime.class, 5, true, false, (livingEntity, level) -> {
              var config = this.level().am$get(Slimes.CONFIG);
              if (!config.available || !config.merge) return false;
              if (this.andromeda$mergeCD > 0) return false;
              float d = livingEntity.distanceTo(this);
              return d <= 6
                  && (getSize() <= config.maxMerge && ((Slime) livingEntity).getSize() < getSize());
            }));
  }

  @Inject(at = @At("TAIL"), method = "push")
  private void andromeda$push(Entity entity, CallbackInfo ci) {
    var config = this.level().am$get(Slimes.CONFIG);
    if (!config.available || !config.merge) return;

    if (getTarget() instanceof Slime slime && slime == entity && this.andromeda$mergeCD == 0) {
      int largest = Math.max(slime.getSize(), getSize());
      int size = (int) Math.max(largest, Math.round(slime.getSize() * 0.75 + getSize() * 0.75));

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
  private void andromeda$writeNbt(ValueOutput output, CallbackInfo ci) {
    output.putInt("AM-MergeCD", Math.max(this.andromeda$mergeCD, 0));
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(ValueInput input, CallbackInfo ci) {
    this.andromeda$mergeCD = input.getIntOr("AM-MergeCD", MathUtil.nextInt(700, 2000));
  }
}
