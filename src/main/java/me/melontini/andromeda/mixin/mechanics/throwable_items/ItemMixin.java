package me.melontini.andromeda.mixin.mechanics.throwable_items;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import me.melontini.andromeda.modules.mechanics.throwable_items.client.Client;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
class ItemMixin {
    @Unique
    private static final ThrowableItems am$thritm = ModuleManager.quick(ThrowableItems.class);

    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void andromeda$tooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (!am$thritm.config().tooltip) return;

        if (world != null && world.isClient() && Client.hasTooltip(stack.getItem())) {
            tooltip.add(TextUtil.translatable("tooltip.andromeda.throwable_item").formatted(Formatting.GRAY));
        }
    }
}
