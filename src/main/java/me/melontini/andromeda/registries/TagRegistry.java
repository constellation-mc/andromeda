package me.melontini.andromeda.registries;

import me.melontini.andromeda.util.AndromedaLog;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static me.melontini.andromeda.util.SharedConstants.MODID;

public class TagRegistry {
    public static class VillagerGifts {
        public static final TagKey<Item> MAJOR_POSITIVE = TagKey.of(Registry.ITEM_KEY, new Identifier(MODID, "villager_gifts/major_positive"));

        public static final TagKey<Item> MINOR_POSITIVE = TagKey.of(Registry.ITEM_KEY, new Identifier(MODID, "villager_gifts/minor_positive"));

        public static final TagKey<Item> MINOR_NEGATIVE = TagKey.of(Registry.ITEM_KEY, new Identifier(MODID, "villager_gifts/major_negative"));

        public static final TagKey<Item> MAJOR_NEGATIVE = TagKey.of(Registry.ITEM_KEY, new Identifier(MODID, "villager_gifts/minor_negative"));

        public static void register() {
        }
    }

    public static void register() {
        VillagerGifts.register();
        AndromedaLog.info("TagRegistry init complete!");
    }
}
