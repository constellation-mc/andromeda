package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.common.util.TagUtil;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

import static me.melontini.andromeda.common.registries.Common.id;

public class PickupTag {

    public static final TagKey<Item> ZOMBIES_PICKUP = TagKey.of(TagUtil.key("item"), id("zombies_pick_up"));
}
