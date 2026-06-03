package dev.zenfyr.andromeda.modules.entities.villagers_follow_emeralds;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public class VillagerTemptGoal extends TemptGoal {

  public static final TagKey<Item> TEMPTING =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("tempting_for_villagers"));

  public VillagerTemptGoal(Villager entity, double speed, Ingredient food, boolean canBeScared) {
    super(entity, speed, food, canBeScared);
  }

  @Override
  public boolean canUse() {
    if (this.mob.level.isClientSide()) return false;
    if (!this.mob.level.am$get(VillagersFollowEmeralds.CONFIG).available) return false;

    if (this.calmDown > 0) {
      --this.calmDown;
      return false;
    } else {
      if (mob.getBrain().isActive(Activity.PANIC)
          || mob.getBrain().isActive(Activity.REST)
          || mob.getBrain().isActive(Activity.HIDE)) {
        return false;
      } else {
        this.player = getServerLevel(this.mob)
            .getNearestPlayer(
                this.targetingConditions.range(this.mob.getAttributeValue(Attributes.TEMPT_RANGE)),
                this.mob);
        return player != null;
      }
    }
  }
}
