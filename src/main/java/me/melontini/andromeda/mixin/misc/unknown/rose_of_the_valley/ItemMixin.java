package me.melontini.andromeda.mixin.misc.unknown.rose_of_the_valley;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.items.RoseOfTheValley;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
@Feature("unknown")
class ItemMixin {
    @Inject(at = @At("HEAD"), method = "onClicked", cancellable = true)
    private void andromeda$onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (Config.get().unknown)
            if (clickType == ClickType.RIGHT && stack.isOf(Items.LILY_OF_THE_VALLEY) && otherStack.isOf(Items.DIAMOND)) {
                //I mean .....yeah
                RoseOfTheValley.handleClick(stack, otherStack, player);
                cir.setReturnValue(true);
            }
    }
}
