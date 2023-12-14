package me.melontini.andromeda.modules.misc.unknown.mixin.rose_of_the_valley;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.misc.unknown.RoseOfTheValley;
import me.melontini.andromeda.modules.misc.unknown.Unknown;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
class ItemMixin {
    @Unique
    private static final Unknown am$unk = ModuleManager.quick(Unknown.class);
    @Inject(at = @At("HEAD"), method = "onClicked", cancellable = true)
    private void andromeda$onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (am$unk.enabled())
            if (clickType == ClickType.RIGHT && stack.isOf(Items.LILY_OF_THE_VALLEY) && otherStack.isOf(Items.DIAMOND)) {
                //I mean .....yeah
                RoseOfTheValley.handleClick(stack, otherStack, player);
                cir.setReturnValue(true);
            }
    }
}
