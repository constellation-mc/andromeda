package dev.zenfyr.andromeda.modules.items.balanced_mending.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

  @ModifyReturnValue(method = "getBaseRepairCost", at = @At("RETURN"))
  private int andromeda$getRepairCost(int original) {
    if (original >= 52
        && EnchantmentHelper.getEnchantments((ItemStack) (Object) this)
            .containsKey(Enchantments.MENDING)) {
      return 52;
    }
    return original;
  }
}
