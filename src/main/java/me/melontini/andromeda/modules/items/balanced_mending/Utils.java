package me.melontini.andromeda.modules.items.balanced_mending;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

public class Utils {

  public static boolean hasMending(ItemStack stack) {
    var component =
        stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

    for (RegistryEntry<Enchantment> enchantment : component.getEnchantments()) {
      if (enchantment.matchesKey(Enchantments.MENDING)) return true;
    }
    return false;
  }
}
