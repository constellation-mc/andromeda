package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {

    @ModifyExpressionValue(method = "chooseEquipmentWith", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/ComponentMap;contains(Lnet/minecraft/component/ComponentType;)Z"))
    private static boolean andIsNotMending(boolean original, @Local RegistryEntry<Enchantment> entry) {
        return original && !entry.matchesKey(Enchantments.MENDING);
    }
}
