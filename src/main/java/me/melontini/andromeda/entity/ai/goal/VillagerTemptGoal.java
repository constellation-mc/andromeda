package me.melontini.andromeda.entity.ai.goal;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.recipe.Ingredient;

public class VillagerTemptGoal extends TemptGoal {

    public VillagerTemptGoal(VillagerEntity entity, double speed, Ingredient food, boolean canBeScared) {
        super(entity, speed, food, canBeScared);
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        } else {
            if (mob.getBrain().hasActivity(Activity.PANIC) || mob.getBrain().hasActivity(Activity.REST) || mob.getBrain().hasActivity(Activity.HIDE)) {
                return false;
            } else {
                this.closestPlayer = this.mob.getWorld().getClosestPlayer(this.predicate, this.mob);
                return closestPlayer != null;
            }
        }
    }
}
