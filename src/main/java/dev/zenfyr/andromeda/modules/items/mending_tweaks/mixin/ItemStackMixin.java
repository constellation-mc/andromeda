package dev.zenfyr.andromeda.modules.items.mending_tweaks.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

  // TODO no idea
  //  @ModifyReturnValue(method = "getBaseRepairCost", at = @At("RETURN"))
  //  private int andromeda$getRepairCost(int original) {
  //    if (original >= 52
  //        && EnchantmentHelper.getEnchantments((ItemStack) (Object) this)
  //            .containsKey(Enchantments.MENDING)) {
  //      return 52;
  //    }
  //    return original;
  //  }
}
