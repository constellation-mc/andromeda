package me.melontini.andromeda.mixin.items.tooltips.recovery_compass;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.items.tooltips.Tooltips;
import me.melontini.andromeda.util.MiscUtil;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
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
    private static final Tooltips am$tooltips = ModuleManager.quick(Tooltips.class);
    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void andromeda$tooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (!am$tooltips.config().recoveryCompass) return;

        if (world != null) if (world.isClient) {
            if (stack.getItem() == Items.RECOVERY_COMPASS && MinecraftClient.getInstance().player != null) {
                var optional = MinecraftClient.getInstance().player.getLastDeathPos();
                if (optional.isPresent()) {
                    GlobalPos globalPos = optional.get();

                    double dist;
                    if (world.getRegistryKey() == globalPos.getDimension()) {
                        Vec3d compassPos = new Vec3d(globalPos.getPos().getX() + 0.5, globalPos.getPos().getY() + 0.5, globalPos.getPos().getZ() + 0.5);
                        dist = MiscUtil.horizontalDistanceTo(MinecraftClient.getInstance().player.getPos(), compassPos);
                    } else {
                        dist = MathStuff.threadRandom().nextGaussian() * 0.1;
                    }
                    tooltip.add(TextUtil.translatable("tooltip.andromeda.compass.recovery", String.format("%.1f", dist)).formatted(Formatting.GRAY));
                }
            }
        }
    }
}
