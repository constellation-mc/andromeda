package dev.zenfyr.andromeda.modules.items.mending_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.zenfyr.andromeda.modules.items.mending_tweaks.Utils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {

  public AnvilMenuMixin(
      @Nullable MenuType<?> menuType,
      int i,
      Inventory inventory,
      ContainerLevelAccess containerLevelAccess,
      ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
    super(menuType, i, inventory, containerLevelAccess, itemCombinerMenuSlotDefinition);
  }

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"),
      method = {"createResult", "createResultInternal"})
  private Object andromeda$setCostLimit(
      ItemStack instance,
      DataComponentType<Integer> dataComponentType,
      Object o,
      Operation<Object> original) {
    if (!DataComponents.REPAIR_COST.equals(dataComponentType))
      return original.call(instance, dataComponentType, o);

    int value = (int) original.call(instance, dataComponentType, o);
    if (value >= 52) {
      return Utils.hasMending(instance) ? 52 : value;
    }
    return value;
  }

  @ModifyExpressionValue(
      method = {"createResult", "createResultInternal"},
      at = @At(value = "CONSTANT", args = "intValue=40"))
  private int andromeda$setRepairLimit(int constant) {
    if (!this.getSlot(1).getItem().is(Items.ENCHANTED_BOOK))
      if (Utils.hasMending(this.getSlot(0).getItem())) {
        return Integer.MAX_VALUE;
      }
    return constant;
  }
}
