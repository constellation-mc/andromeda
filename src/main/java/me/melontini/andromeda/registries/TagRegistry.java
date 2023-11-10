package me.melontini.andromeda.registries;

import me.melontini.andromeda.util.annotations.AndromedaRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import static me.melontini.andromeda.registries.Common.id;

@AndromedaRegistry("andromeda:tag_registry")
public class TagRegistry {

    public static class VillagerGifts {

        public static final TagKey<Item> MAJOR_POSITIVE = TagKey.of(key("item"), id("villager_gifts/major_positive"));

        public static final TagKey<Item> MINOR_POSITIVE = TagKey.of(key("item"), id("villager_gifts/minor_positive"));

        public static final TagKey<Item> MINOR_NEGATIVE = TagKey.of(key("item"), id("villager_gifts/major_negative"));

        public static final TagKey<Item> MAJOR_NEGATIVE = TagKey.of(key("item"), id("villager_gifts/minor_negative"));
    }

    public static final TagKey<Item> ZOMBIES_PICKUP = TagKey.of(key("item"), id("zombies_pickup"));

    private static RegistryKey<Registry<Item>> key(String key) {
        return RegistryKey.ofRegistry(Identifier.tryParse(key));
    }
}
