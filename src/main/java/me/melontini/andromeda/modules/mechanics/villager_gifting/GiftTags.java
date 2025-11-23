package me.melontini.andromeda.modules.mechanics.villager_gifting;

import static me.melontini.andromeda.common.Andromeda.id;

import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.gossip.GossipType;

public class GiftTags {
  public static final TagKey<Item> MAJOR_POSITIVE =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("villager_gifts/major_positive"));
  public static final TagKey<Item> MINOR_POSITIVE =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("villager_gifts/minor_positive"));
  public static final TagKey<Item> MINOR_NEGATIVE =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("villager_gifts/major_negative"));
  public static final TagKey<Item> MAJOR_NEGATIVE =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("villager_gifts/minor_negative"));

  public static final Map<TagKey<Item>, Action> ACTION_MAP = Map.of(
      MAJOR_POSITIVE, new Action((byte) 14, GossipType.MAJOR_POSITIVE),
      MINOR_POSITIVE, new Action((byte) 14, GossipType.MINOR_POSITIVE),
      MAJOR_NEGATIVE, new Action((byte) 13, GossipType.MAJOR_NEGATIVE),
      MINOR_NEGATIVE, new Action((byte) 13, GossipType.MINOR_NEGATIVE));

  public record Action(byte status, GossipType type) {}
}
