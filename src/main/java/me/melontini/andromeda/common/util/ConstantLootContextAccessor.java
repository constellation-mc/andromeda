package me.melontini.andromeda.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

public interface ConstantLootContextAccessor {

    static Supplier<LootContext> get(Entity entity) {
        return ((ConstantLootContextAccessor) entity).andromeda$getLootContext();
    }

    Supplier<LootContext> andromeda$getLootContext();
}
