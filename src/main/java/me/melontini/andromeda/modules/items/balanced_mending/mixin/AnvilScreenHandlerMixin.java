package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.modules.items.balanced_mending.Utils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilScreenHandler.class)
abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

  public AnvilScreenHandlerMixin(
      @org.jetbrains.annotations.Nullable ScreenHandlerType<?> type,
      int syncId,
      PlayerInventory playerInventory,
      ScreenHandlerContext context) {
    super(type, syncId, playerInventory, context);
  }

  @ModifyExpressionValue(
      method = "updateResult",
      at = @At(value = "CONSTANT", args = "intValue=40"))
  private int andromeda$setRepairLimit(int constant) {
    if (!this.getSlot(1).getStack().isOf(Items.ENCHANTED_BOOK))
      if (Utils.hasMending(this.getSlot(0).getStack())) {
        return Integer.MAX_VALUE;
      }
    return constant;
  }
}
