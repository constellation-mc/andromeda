package me.melontini.andromeda.mixin.blocks.better_fletching_table;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
@MixinRelatedConfigOption("usefulFletching")
public class ItemMixin {
    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void andromeda$tooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (Andromeda.CONFIG.usefulFletching) if (stack.getItem() instanceof BowItem) {
            int a = NbtUtil.getInt(stack.getNbt(), "AM-Tightened", 0);
            if (a > 0) {
                tooltip.add(TextUtil.translatable("tooltip.andromeda.bow.tight", a).formatted(Formatting.GRAY));
            }
        }
    }
}
