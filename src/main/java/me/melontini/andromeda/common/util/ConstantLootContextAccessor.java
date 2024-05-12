package me.melontini.andromeda.common.util;

import com.google.common.base.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;

public interface ConstantLootContextAccessor {

    static Supplier<LootContext> get(Entity entity) {
        return ((ConstantLootContextAccessor) entity).andromeda$getLootContext();
    }

    Supplier<LootContext> andromeda$getLootContext();
}
