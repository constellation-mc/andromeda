package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.util.TagUtil;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

import static me.melontini.andromeda.registries.Common.id;

public class PickupTag {

    public static final TagKey<Item> ZOMBIES_PICKUP = TagKey.of(TagUtil.key("item"), id("zombies_pickup"));

    public static void init() {

    }
}
