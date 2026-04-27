package dev.zenfyr.andromeda.modules.items.mending_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.andromeda.modules.items.mending_tweaks.Utils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {

  public AnvilScreenHandlerMixin(
      @Nullable MenuType<?> menuType,
      int i,
      Inventory inventory,
      ContainerLevelAccess containerLevelAccess,
      ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
    super(menuType, i, inventory, containerLevelAccess, itemCombinerMenuSlotDefinition);
  }

  @ModifyExpressionValue(
      method = "createResult",
      at = @At(value = "CONSTANT", args = "intValue=40"))
  private int andromeda$setRepairLimit(int constant) {
    if (!this.getSlot(1).getItem().is(Items.ENCHANTED_BOOK))
      if (Utils.hasMending(this.getSlot(0).getItem())) {
        return Integer.MAX_VALUE;
      }
    return constant;
  }
}
