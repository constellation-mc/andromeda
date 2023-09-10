package me.melontini.andromeda.registries;

import me.melontini.andromeda.util.AndromedaLog;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Objects;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.share;

public class TagRegistry {

    private static TagRegistry INSTANCE;

    private final VillagerGifts GIFTS = new VillagerGifts();

    public static class VillagerGifts {

        public final TagKey<Item> MAJOR_POSITIVE = TagKey.of(key("item"), id("villager_gifts/major_positive"));

        public final TagKey<Item> MINOR_POSITIVE = TagKey.of(key("item"), id("villager_gifts/minor_positive"));

        public final TagKey<Item> MINOR_NEGATIVE = TagKey.of(key("item"), id("villager_gifts/major_negative"));

        public final TagKey<Item> MAJOR_NEGATIVE = TagKey.of(key("item"), id("villager_gifts/minor_negative"));
    }

    public final TagKey<Item> ZOMBIES_PICKUP = TagKey.of(key("item"), id("zombies_pickup"));

    public VillagerGifts getGifts() {
        return GIFTS;
    }

    public static TagRegistry get() {
        return Objects.requireNonNull(INSTANCE, "%s requested too early!".formatted(INSTANCE.getClass()));
    }

    public static void register() {
        if (INSTANCE != null) throw new IllegalStateException("%s already initialized!".formatted(INSTANCE.getClass()));

        INSTANCE = new TagRegistry();
        share("andromeda:tag_registry", INSTANCE);
        AndromedaLog.info("%s init complete!".formatted(INSTANCE.getClass().getSimpleName()));
    }

    private static RegistryKey<Registry<Item>> key(String key) {
        return RegistryKey.ofRegistry(Identifier.tryParse(key));
    }
}
