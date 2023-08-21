package me.melontini.andromeda.entity.ai.goal;

import net.minecraft.entity.LivingEntity;

public interface ItemThrowerMob<T extends LivingEntity> {

    void am$throwItem(LivingEntity target, float pullProgress);

}
