package me.melontini.andromeda.mixin.items.better_names;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.items.better_names.BetterNames;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Unique
    private static final BetterNames am$sbin = ModuleManager.quick(BetterNames.class);
    @Shadow public abstract int getMaxDamage();
    @Shadow public abstract Item getItem();
    @Shadow public abstract int getCount();
    @Shadow public abstract int getDamage();
    @Shadow public abstract Rarity getRarity();

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BEFORE), method = "getTooltip", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void andromeda$getTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list, MutableText mutableText) {
        if (!am$sbin.config().enabled) return;

        if (!this.getItem().isDamageable()) {
            if (this.getCount() > 1)
                mutableText.append(TextUtil.literal(" x" + this.getCount()).formatted(getRarity().formatting));
        } else {
            if (this.getDamage() > 0)
                mutableText.append(TextUtil.literal(" " + ((this.getMaxDamage() - this.getDamage()) * 100 / this.getMaxDamage()) + "%").formatted(getRarity().formatting));
        }
    }
}
