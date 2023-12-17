package me.melontini.andromeda.modules.world.crop_temperature.mixin;

import me.melontini.andromeda.modules.world.crop_temperature.Content;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperatureData;
import net.minecraft.block.Block;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
class BoneMealItemMixin {

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    private void andromeda$useOnFertilizable(ItemUsageContext ctx, CallbackInfoReturnable<ActionResult> cir) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        if (world.getGameRules().getBoolean(Content.AFFECT_BONE_MEAL) && !world.isClient()) {
            Block block = world.getBlockState(pos).getBlock();
            if (!PlantTemperatureData.roll(block, world.getBiome(pos).value().getTemperature())) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
