package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static java.util.Objects.requireNonNull;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

public class ThrowableItemAttackGoal<T extends MobEntity> extends Goal {

    private final ItemThrowerMob<T> owner;
    private final MobEntity mob;
    @Nullable private LivingEntity target;

    private final double mobSpeed;
    private final float minRange;
    private final float range;

    private int seenTargetTicks;
    private int updateCountdownTicks;

    public ThrowableItemAttackGoal(ItemThrowerMob<T> mob, double mobSpeed, float range) {
        this(mob, mobSpeed, 0, range);
    }

    public ThrowableItemAttackGoal(ItemThrowerMob<T> mob, double mobSpeed, float minRange, float range) {
        this.owner = mob;
        this.mob = (MobEntity) mob;
        this.mobSpeed = mobSpeed;
        this.minRange = minRange;
        this.range = range;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (requireNonNull(mob.world.getServer()).dm$getReloader(RELOADER).hasBehaviors(this.mob.getMainHandStack())) {
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

    @Override
    public boolean shouldContinue() {
        return this.canStart() || (requireNonNull(this.target).isAlive() && !this.mob.getNavigation().isIdle());
    }

    @Override
    public void start() {
        super.start();
        this.mob.setCurrentHand(Hand.MAIN_HAND);
        this.mob.setAttacking(true);
    }

    @Override
    public void stop() {
        this.target = null;
        this.seenTargetTicks = 0;
        this.updateCountdownTicks = -1;
        this.mob.clearActiveItem();
        this.mob.setAttacking(false);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
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
            this.owner.am$throwItem(requireNonNull(this.target), g);
            this.updateCountdownTicks = MathHelper.floor(f * getInterval());
        } else if (this.updateCountdownTicks < 0) {
            this.updateCountdownTicks = MathHelper.floor(getInterval());
        }
    }

    public double getInterval() {
        return mob.world.am$get(ThrowableItems.CONFIG).zombieThrowInterval.asDouble(ConstantLootContextAccessor.get(mob));
    }
}
