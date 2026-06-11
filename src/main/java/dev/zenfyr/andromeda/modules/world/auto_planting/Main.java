package dev.zenfyr.andromeda.modules.world.auto_planting;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class Main {
  public static final TagKey<Item> ITEM_LIST =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("auto_planting/items"));
}
