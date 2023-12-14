package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.items.balanced_mending.BalancedMending;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Unique
    private static final BalancedMending am$balmend = ModuleManager.quick(BalancedMending.class);
    @ModifyReturnValue(method = "getRepairCost", at = @At("RETURN"))
    private int andromeda$getRepairCost(int original) {
        if (am$balmend.enabled() && original >= 52 && EnchantmentHelper.get((ItemStack) (Object) this).containsKey(Enchantments.MENDING)) {
            return 52;
        }
        return original;
    }
}
