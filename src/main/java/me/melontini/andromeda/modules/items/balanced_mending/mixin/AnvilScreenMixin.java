package me.melontini.andromeda.modules.items.balanced_mending.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.modules.items.balanced_mending.BalancedMending;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(AnvilScreen.class)
abstract class AnvilScreenMixin extends HandledScreen<AnvilScreenHandler> {
    @Unique
    private static final BalancedMending am$balmend = ModuleManager.quick(BalancedMending.class);
    public AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @ModifyExpressionValue(method = "drawForeground", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int andromeda$setRepairLimit(int constant) {
        if (am$balmend.config().enabled)
            if (!this.handler.getSlot(1).getStack().isOf(Items.ENCHANTED_BOOK))
                if (EnchantmentHelper.get(this.handler.getSlot(0).getStack()).containsKey(Enchantments.MENDING)) {
                    return Integer.MAX_VALUE;
                }
        return constant;
    }
}
