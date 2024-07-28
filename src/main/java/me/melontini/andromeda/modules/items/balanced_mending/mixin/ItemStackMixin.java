package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.modules.items.balanced_mending.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    //TODO I've been massively trolled
    //@ModifyReturnValue(method = "getRepairCost", at = @At("RETURN"))
    //private int andromeda$getRepairCost(int original) {
    //    if (original >= 52 && Utils.hasMending((ItemStack) (Object) this)) {
    //        return 52;
    //    }
    //    return original;
    //}
}
