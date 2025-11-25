package dev.zenfyr.andromeda.modules.mechanics.throwable_items;

import net.minecraft.world.entity.LivingEntity;

public interface ItemThrowerMob<T extends LivingEntity> {

  void am$throwItem(LivingEntity target, float pullProgress);

  int am$cooldown();
}
