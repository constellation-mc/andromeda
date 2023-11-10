package me.melontini.andromeda.mixin.items.balanced_mending;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
@Feature("balancedMending")
abstract class ItemStackMixin {
    @ModifyReturnValue(method = "getRepairCost", at = @At("RETURN"))
    private int andromeda$getRepairCost(int original) {
        if (Config.get().balancedMending && original >= 52 && EnchantmentHelper.get((ItemStack) (Object) this).containsKey(Enchantments.MENDING)) {
            return 52;
        }
        return original;
    }
}