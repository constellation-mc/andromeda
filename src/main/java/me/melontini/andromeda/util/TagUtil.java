package me.melontini.andromeda.util;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class TagUtil {
    public static RegistryKey<Registry<Item>> key(String key) {
        return RegistryKey.ofRegistry(Identifier.tryParse(key));
    }
}
