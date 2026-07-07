package dev.zenfyr.andromeda.modules.world.auto_planting.mixin;

import dev.zenfyr.andromeda.modules.world.auto_planting.AutoPlanting;
import dev.zenfyr.andromeda.modules.world.auto_planting.AutoPlantingMain;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {

  @Inject(at = @At("HEAD"), method = "tick")
  public void andromeda$tryPlant(CallbackInfo ci) {
    ItemEntity entity = (ItemEntity) (Object) this;
    ItemStack stack = entity.getItem();
    BlockPos pos = entity.blockPosition();
    Level level = entity.level();

    if (level.isClientSide()) return;
    if (!(stack.getItem() instanceof BlockItem blockItem)
        || !(blockItem.getBlock() instanceof VegetationBlock)) return;

    if (entity.tickCount % MathUtil.nextInt(20, 101) != 0) return;
    var config = level.am$get(AutoPlanting.CONFIG);
    if (!config.available) return;
    if (!level.getFluidState(pos).isEmpty()) return;
    if (config.blacklistMode == stack.is(AutoPlantingMain.ITEM_LIST)) return;

    blockItem.place(new BlockPlaceContext(
        level,
        null,
        null,
        stack,
        level.clip(new ClipContext(
            Vec3.atLowerCornerWithOffset(pos, 0.5, 0.5, 0.5),
            Vec3.atLowerCornerWithOffset(pos, 0.5, -0.5, 0.5),
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.ANY,
            entity))));
  }
}
