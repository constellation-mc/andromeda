package me.melontini.andromeda.common.util;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public interface ConstantLootContextAccessor {

  static Supplier<LootContext> get(Entity entity) {
    return ((ConstantLootContextAccessor) entity).andromeda$getLootContext();
  }

  Supplier<LootContext> andromeda$getLootContext();
}
