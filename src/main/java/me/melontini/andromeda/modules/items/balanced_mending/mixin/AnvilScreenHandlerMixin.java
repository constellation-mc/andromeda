package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {

  public AnvilScreenHandlerMixin(
          @org.jetbrains.annotations.Nullable MenuType<?> type,
          int syncId,
          Inventory playerInventory,
          ContainerLevelAccess context) {
    super(type, syncId, playerInventory, context);
  }

  @ModifyExpressionValue(
      method = "createResult",
      at = @At(value = "CONSTANT", args = "intValue=40"))
  private int andromeda$setRepairLimit(int constant) {
    if (!this.getSlot(1).getItem().is(Items.ENCHANTED_BOOK))
      if (EnchantmentHelper.getEnchantments(this.getSlot(0).getItem()).containsKey(Enchantments.MENDING)) {
        return Integer.MAX_VALUE;
      }
    return constant;
  }
}
