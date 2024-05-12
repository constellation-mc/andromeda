package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import static me.melontini.andromeda.common.Andromeda.id;

public class PickupTag {

    public static final TagKey<Item> ZOMBIES_PICKUP = TagKey.of(Registries.ITEM.getKey(), id("zombies_pick_up"));
}
