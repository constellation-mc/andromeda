package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import static me.melontini.andromeda.common.Andromeda.id;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PickupTag {

  public static final TagKey<Item> ZOMBIES_PICKUP =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("zombies_pick_up"));
}
