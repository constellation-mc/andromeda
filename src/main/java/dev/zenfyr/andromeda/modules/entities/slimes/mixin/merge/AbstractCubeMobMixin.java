package dev.zenfyr.andromeda.modules.entities.slimes.mixin.merge;

import dev.zenfyr.andromeda.modules.entities.slimes.SlimeMergeDuck;
import dev.zenfyr.andromeda.modules.entities.slimes.Slimes;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCubeMob.class)
abstract class AbstractCubeMobMixin extends AgeableMob implements SlimeMergeDuck {

  @Shadow
  public abstract int getSize();

  @Shadow
  public abstract void setSize(int size, boolean updateHealth);

  @Unique private int andromeda$mergeCD = MathUtil.nextInt(700, 2000);

  protected AbstractCubeMobMixin(EntityType<? extends AgeableMob> type, Level level) {
    super(type, level);
  }

  @Inject(at = @At("TAIL"), method = "push")
  private void andromeda$push(Entity entity, CallbackInfo ci) {
    if (!((Object) this instanceof Slime)) return;
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
  private void andromeda$writeNbt(ValueOutput valueOutput, CallbackInfo ci) {
    valueOutput.putInt("AM-MergeCD", Math.max(this.andromeda$mergeCD, 0));
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(ValueInput valueInput, CallbackInfo ci) {
    this.andromeda$mergeCD = valueInput.getIntOr("AM-MergeCD", MathUtil.nextInt(700, 2000));
  }

  @Override
  public int andromeda$mergeCD() {
    return this.andromeda$mergeCD;
  }
}
