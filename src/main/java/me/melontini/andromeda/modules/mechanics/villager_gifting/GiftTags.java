package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.common.util.TagUtil;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.village.VillageGossipType;

import java.util.Map;

import static me.melontini.andromeda.common.registries.Common.id;

public class GiftTags {
    public static final TagKey<Item> MAJOR_POSITIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/major_positive"));
    public static final TagKey<Item> MINOR_POSITIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/minor_positive"));
    public static final TagKey<Item> MINOR_NEGATIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/major_negative"));
    public static final TagKey<Item> MAJOR_NEGATIVE = TagKey.of(TagUtil.key("item"), id("villager_gifts/minor_negative"));

    public static final Map<TagKey<Item>, Action> ACTION_MAP = Map.of(
            MAJOR_POSITIVE, new Action((byte) 14, VillageGossipType.MAJOR_POSITIVE),
            MINOR_POSITIVE, new Action((byte) 14, VillageGossipType.MINOR_POSITIVE),
            MAJOR_NEGATIVE, new Action((byte) 13, VillageGossipType.MAJOR_NEGATIVE),
            MINOR_NEGATIVE, new Action((byte) 13, VillageGossipType.MINOR_NEGATIVE)
    );

    public record Action(byte status, VillageGossipType type) {}
}
