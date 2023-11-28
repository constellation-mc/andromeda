package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.util.TagUtil;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

import static me.melontini.andromeda.registries.Common.id;

public class GiftTags {
    public static final TagKey<Item> MAJOR_POSITIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/major_positive"));
    public static final TagKey<Item> MINOR_POSITIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/minor_positive"));
    public static final TagKey<Item> MINOR_NEGATIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/major_negative"));
    public static final TagKey<Item> MAJOR_NEGATIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/minor_negative"));
}
