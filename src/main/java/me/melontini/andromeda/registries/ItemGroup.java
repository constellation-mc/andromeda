package me.melontini.andromeda.registries;

import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.minecraft.util.registry.Registry;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.start;

public class ItemGroup {
    @SuppressWarnings("unused")
    public static final Keeper<net.minecraft.item.ItemGroup> GROUP = start(() -> ContentBuilder.ItemGroupBuilder.create(id("group"))
            .entries(entries -> Registry.ITEM.streamEntries()
                    .filter(ref -> ref.getKey().map(k -> k.getValue().getNamespace().equals("andromeda")).orElse(false))
                    .forEach(ref -> entries.add(ref.value())))
            .displayName(AndromedaTexts.ITEM_GROUP_NAME));
}
