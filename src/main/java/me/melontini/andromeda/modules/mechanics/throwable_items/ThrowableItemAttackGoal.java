package me.melontini.andromeda.modules.mechanics.throwable_items;

import static java.util.Objects.requireNonNull;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public class ThrowableItemAttackGoal<T extends Mob> extends Goal {

  private final ItemThrowerMob<T> owner;
  private final Mob mob;

  @Nullable private LivingEntity target;

  private final double mobSpeed;
  private final float minRange;
  private final float range;

  private int seenTargetTicks;
  private int updateCountdownTicks;

  public ThrowableItemAttackGoal(ItemThrowerMob<T> mob, double mobSpeed, float range) {
    this(mob, mobSpeed, 0, range);
  }

  public ThrowableItemAttackGoal(
      ItemThrowerMob<T> mob, double mobSpeed, float minRange, float range) {
    this.owner = mob;
    this.mob = (Mob) mob;
    this.mobSpeed = mobSpeed;
    this.minRange = minRange;
    this.range = range;
    this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
  }

  @Override
  public boolean canUse() {
    if (requireNonNull(mob.level.getServer())
        .dm$getReloader(RELOADER)
        .hasBehaviors(this.mob.getMainHandItem())) {
      LivingEntity livingEntity = this.mob.getTarget();
      if (livingEntity != null && livingEntity.isAlive() && this.owner.am$cooldown() <= 0) {
        double d = this.mob.distanceTo(livingEntity);
        Path path = this.mob.getNavigation().getPath();
        if ((d <= this.range && d >= this.minRange) || (path != null && !path.canReach())) {
          this.target = livingEntity;
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canContinueToUse() {
    return this.canUse()
        || (requireNonNull(this.target).isAlive() && !this.mob.getNavigation().isDone());
  }

  @Override
  public void start() {
    super.start();
    this.mob.startUsingItem(InteractionHand.MAIN_HAND);
    this.mob.setAggressive(true);
  }

  @Override
  public void stop() {
    this.target = null;
    this.seenTargetTicks = 0;
    this.updateCountdownTicks = -1;
    this.mob.stopUsingItem();
    this.mob.setAggressive(false);
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    double d = this.mob.distanceTo(this.target);
    boolean bl = this.mob.getSensing().hasLineOfSight(this.target);
    if (bl) {
      ++this.seenTargetTicks;
    } else {
      this.seenTargetTicks = 0;
    }

    if (!(d > this.range) && this.seenTargetTicks >= 5) this.mob.getNavigation().stop();
    else this.mob.getNavigation().moveTo(this.target, this.mobSpeed);

    this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
    if (--this.updateCountdownTicks == 0) {
      if (!bl) {
        return;
      }

      float f = (float) Math.sqrt(d) / this.range;
      float g = Mth.clamp(f, 0.1F, 1.0F);
      this.owner.am$throwItem(requireNonNull(this.target), g);
      this.updateCountdownTicks = Mth.floor(f * getInterval());
    } else if (this.updateCountdownTicks < 0) {
      this.updateCountdownTicks = Mth.floor(getInterval());
    }
  }

  public double getInterval() {
    return mob.level.am$get(ThrowableItems.CONFIG).zombieThrowInterval;
  }
}
