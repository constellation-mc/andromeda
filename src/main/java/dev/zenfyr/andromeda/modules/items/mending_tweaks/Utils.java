package dev.zenfyr.andromeda.modules.items.mending_tweaks;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class Utils {

  public static boolean hasMending(ItemStack stack) {
    var component = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

    for (Holder<Enchantment> enchantmentHolder : component.keySet()) {
      if (enchantmentHolder.is(Enchantments.MENDING)) return true;
    }
    return false;
  }
}
