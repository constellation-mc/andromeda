package me.melontini.andromeda.modules.entities.slimes.mixin.merge;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.entities.slimes.Slimes;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import me.melontini.dark_matter.api.data.nbt.NbtUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
abstract class SlimeEntityMixin extends MobEntity {

  @Shadow
  public abstract int getSize();

  @Shadow
  public abstract void setSize(int size, boolean heal);

  @Unique private int andromeda$mergeCD = MathUtil.nextInt(700, 2000);

  protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "initGoals")
  private void andromeda$newGoal(CallbackInfo ci) {
    var config = this.world.am$get(Slimes.CONFIG);
    var supplier = ConstantLootContextAccessor.get(this);
    this.targetSelector.add(
        2,
        new ActiveTargetGoal<>(
            (SlimeEntity) (Object) this, SlimeEntity.class, 5, true, false, livingEntity -> {
              if (!config.available.asBoolean(supplier)) return false;
              var supplier1 = Memoize.supplier(
                  LootContextUtil.entity(world, livingEntity.getPos(), livingEntity, null, this));
              if (!config.merge.asBoolean(supplier1)) return false;
              if (this.andromeda$mergeCD > 0) return false;
              float d = livingEntity.distanceTo(this);
              return d <= 6
                  && (getSize() <= config.maxMerge.asInt(supplier1)
                      && ((SlimeEntity) livingEntity).getSize() < getSize());
            }));
  }

  @Inject(at = @At("TAIL"), method = "pushAwayFrom")
  private void andromeda$push(Entity entity, CallbackInfo ci) {
    var config = this.world.am$get(Slimes.CONFIG);
    var supplier = ConstantLootContextAccessor.get(this);
    if (!config.available.asBoolean(supplier)) return;

    if (!config.merge.asBoolean(LootContextUtil.entity(world, entity.getPos(), entity, null, this)))
      return;

    if (getTarget() instanceof SlimeEntity slime
        && slime == entity
        && this.andromeda$mergeCD == 0) {
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

  @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
  private void andromeda$writeNbt(NbtCompound nbt, CallbackInfo ci) {
    nbt.putInt("AM-MergeCD", Math.max(this.andromeda$mergeCD, 0));
  }

  @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
  private void andromeda$readNbt(NbtCompound nbt, CallbackInfo ci) {
    this.andromeda$mergeCD = NbtUtil.getInt(nbt, "AM-MergeCD", MathUtil.nextInt(700, 2000));
  }
}
