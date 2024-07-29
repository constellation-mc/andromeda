package me.melontini.andromeda.modules.mechanics.villager_gifting;

import static me.melontini.andromeda.common.Andromeda.id;

import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.village.VillageGossipType;

public class GiftTags {
  public static final TagKey<Item> MAJOR_POSITIVE =
      TagKey.of(Registries.ITEM.getKey(), id("villager_gifts/major_positive"));
  public static final TagKey<Item> MINOR_POSITIVE =
      TagKey.of(Registries.ITEM.getKey(), id("villager_gifts/minor_positive"));
  public static final TagKey<Item> MINOR_NEGATIVE =
      TagKey.of(Registries.ITEM.getKey(), id("villager_gifts/major_negative"));
  public static final TagKey<Item> MAJOR_NEGATIVE =
      TagKey.of(Registries.ITEM.getKey(), id("villager_gifts/minor_negative"));

  public static final Map<TagKey<Item>, Action> ACTION_MAP = Map.of(
      MAJOR_POSITIVE, new Action((byte) 14, VillageGossipType.MAJOR_POSITIVE),
      MINOR_POSITIVE, new Action((byte) 14, VillageGossipType.MINOR_POSITIVE),
      MAJOR_NEGATIVE, new Action((byte) 13, VillageGossipType.MAJOR_NEGATIVE),
      MINOR_NEGATIVE, new Action((byte) 13, VillageGossipType.MINOR_NEGATIVE));

  public record Action(byte status, VillageGossipType type) {}
}
