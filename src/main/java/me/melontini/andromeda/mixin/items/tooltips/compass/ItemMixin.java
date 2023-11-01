package me.melontini.andromeda.mixin.items.tooltips.compass;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.MiscUtil;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
@Feature("tooltips.compass")
class ItemMixin {
    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void andromeda$tooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (!Config.get().tooltips.compass) return;

        if (world != null) if (world.isClient) {
            if (stack.getItem() == Items.COMPASS && MinecraftClient.getInstance().player != null) {
                boolean lodestone = stack.hasNbt() && CompassItem.hasLodestone(stack);
                GlobalPos globalPos = lodestone ? MiscUtil.createLodestonePos(stack.getNbt()) : MiscUtil.createSpawnPos(world);

                double dist;
                if (globalPos != null && world.getRegistryKey() == globalPos.getDimension()) {
                    Vec3d compassPos = new Vec3d(globalPos.getPos().getX() + 0.5, globalPos.getPos().getY() + 0.5, globalPos.getPos().getZ() + 0.5);
                    dist = MiscUtil.horizontalDistanceTo(MinecraftClient.getInstance().player.getPos(), compassPos);
                } else {
                    dist = MathStuff.threadRandom().nextGaussian() * 0.1;
                }
                tooltip.add(TextUtil.translatable(lodestone ? "tooltip.andromeda.compass.lodestone" : "tooltip.andromeda.compass", String.format("%.1f", dist)).formatted(Formatting.GRAY));
            }
        }
    }
}
