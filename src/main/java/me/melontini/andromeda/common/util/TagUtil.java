package me.melontini.andromeda.common.util;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class TagUtil {
    public static RegistryKey<Registry<Item>> key(String key) {
        return RegistryKey.ofRegistry(Identifier.tryParse(key));
    }
}
