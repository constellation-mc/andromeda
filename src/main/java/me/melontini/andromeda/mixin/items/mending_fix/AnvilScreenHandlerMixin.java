package me.melontini.andromeda.mixin.items.mending_fix;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilScreenHandler.class)
@MixinRelatedConfigOption("balancedMending")
abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    public AnvilScreenHandlerMixin(@org.jetbrains.annotations.Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @ModifyExpressionValue(method = "updateResult", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int andromeda$setRepairLimit(int constant) {
        if (Config.get().balancedMending)
            if (!this.getSlot(1).getStack().isOf(Items.ENCHANTED_BOOK))
                if (EnchantmentHelper.get(this.getSlot(0).getStack()).containsKey(Enchantments.MENDING)) {
                    return Integer.MAX_VALUE;
                }
        return constant;
    }
}
