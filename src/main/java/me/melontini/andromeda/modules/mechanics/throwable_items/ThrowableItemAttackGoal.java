package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class ThrowableItemAttackGoal<T extends MobEntity> extends Goal {

    private final ItemThrowerMob<T> owner;
    private final MobEntity mob;
    private LivingEntity target;

    private final double mobSpeed;
    private final int minInterval;
    private final int maxInterval;
    private final float minRange;
    private final float range;

    private int seenTargetTicks;
    private int updateCountdownTicks;

    public ThrowableItemAttackGoal(ItemThrowerMob<T> mob, double mobSpeed, int intervalTicks, float range) {
        this(mob, mobSpeed, intervalTicks, intervalTicks, 0, range);
    }

    public ThrowableItemAttackGoal(ItemThrowerMob<T> mob, double mobSpeed, int intervalTicks, float minRange, float range) {
        this(mob, mobSpeed, intervalTicks, intervalTicks, minRange, range);
    }

    public ThrowableItemAttackGoal(ItemThrowerMob<T> mob, double mobSpeed, int minInterval, int maxInterval, float minRange, float range) {
        this.owner = mob;
        this.mob = (MobEntity) mob;
        this.mobSpeed = mobSpeed;
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.minRange = minRange;
        this.range = range;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (ItemBehaviorManager.hasBehaviors(this.mob.getMainHandStack().getItem())) {
            LivingEntity livingEntity = this.mob.getTarget();
            if (livingEntity != null && livingEntity.isAlive() && this.owner.am$cooldown() <= 0) {
                double d = this.mob.distanceTo(livingEntity);
                Path path = this.mob.getNavigation().getCurrentPath();
                if ((d <= this.range && d >= this.minRange) || (path != null && !path.reachesTarget())) {
                    this.target = livingEntity;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldContinue() {
        return this.canStart() || this.target.isAlive() && !this.mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setCurrentHand(Hand.MAIN_HAND);
        this.mob.setAttacking(true);
    }

    public void stop() {
        this.target = null;
        this.seenTargetTicks = 0;
        this.updateCountdownTicks = -1;
        this.mob.clearActiveItem();
        this.mob.setAttacking(false);
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        double d = this.mob.distanceTo(this.target);
        boolean bl = this.mob.getVisibilityCache().canSee(this.target);
        if (bl) {
            ++this.seenTargetTicks;
        } else {
            this.seenTargetTicks = 0;
        }

        if (!(d > this.range) && this.seenTargetTicks >= 5) this.mob.getNavigation().stop();
        else this.mob.getNavigation().startMovingTo(this.target, this.mobSpeed);


        this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);
        if (--this.updateCountdownTicks == 0) {
            if (!bl) {
                return;
            }

            float f = (float) Math.sqrt(d) / this.range;
            float g = MathHelper.clamp(f, 0.1F, 1.0F);
            this.owner.am$throwItem(this.target, g);
            this.updateCountdownTicks = MathHelper.floor(f * (this.maxInterval - this.minInterval) + this.minInterval);
        } else if (this.updateCountdownTicks < 0) {
            this.updateCountdownTicks = MathHelper.floor(
                    MathHelper.lerp(Math.sqrt(d) / this.range, this.minInterval, this.maxInterval)
            );
        }
    }

}
