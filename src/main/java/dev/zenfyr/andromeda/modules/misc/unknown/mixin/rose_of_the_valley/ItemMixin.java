package dev.zenfyr.andromeda.modules.misc.unknown.mixin.rose_of_the_valley;

import dev.zenfyr.andromeda.modules.misc.unknown.RoseOfTheValley;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
abstract class ItemMixin {

  @Inject(at = @At("HEAD"), method = "overrideOtherStackedOnMe", cancellable = true)
  private void andromeda$onClicked(
      ItemStack self,
      ItemStack other,
      Slot slot,
      ClickAction clickAction,
      Player player,
      SlotAccess carriedItem,
      CallbackInfoReturnable<Boolean> cir) {
    if (clickAction == ClickAction.SECONDARY
        && self.is(Items.LILY_OF_THE_VALLEY)
        && other.is(Items.DIAMOND)) {
      // I mean .....yeah
      RoseOfTheValley.handleClick(self, other, player);
      cir.setReturnValue(true);
    }
  }
}
