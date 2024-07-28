package me.melontini.andromeda.modules.entities.bee_flower_duplication.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.entities.bee_flower_duplication.BeeFlowerDuplication;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
abstract class BoneMealItemMixin {

    @Inject(at = @At("HEAD"), method = "useOnFertilizable", cancellable = true)
    private static void andromeda$useOnFertilizable(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClient()) return;

        BlockState blockState = world.getBlockState(pos);
        var config = world.am$get(BeeFlowerDuplication.CONFIG);
        var supplier = Memoize.supplier(LootContextUtil.block(world, Vec3d.ofCenter(pos), blockState, stack));
        if (!config.available.asBoolean(supplier) || !config.tallFlowers.asBoolean(supplier)) return;

        if (blockState.getBlock() instanceof TallFlowerBlock) {
            if (ModuleManager.get().getModule("misc.unknown").isPresent() && world.random.nextInt(100) == 0) {
                world.createExplosion(null,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F,
                        false, World.ExplosionSourceType.BLOCK);
            }
            cir.setReturnValue(false);
        }
    }
}
