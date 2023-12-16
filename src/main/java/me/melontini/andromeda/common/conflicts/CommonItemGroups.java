package me.melontini.andromeda.common.conflicts;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;

public class CommonItemGroups {

    public static ItemGroup tools() {
        return Registries.ITEM_GROUP.get(ItemGroups.TOOLS);
    }

    public static ItemGroup combat() {
        return Registries.ITEM_GROUP.get(ItemGroups.COMBAT);
    }

    public static ItemGroup redstone() {
        return Registries.ITEM_GROUP.get(ItemGroups.REDSTONE);
    }

    public static ItemGroup transport() {
        return Registries.ITEM_GROUP.get(ItemGroups.TOOLS);
    }
}
