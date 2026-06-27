package dev.zenfyr.andromeda.modules.world.auto_planting.mixin;

import dev.zenfyr.andromeda.modules.world.auto_planting.AutoPlanting;
import dev.zenfyr.andromeda.modules.world.auto_planting.Main;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {

  @Shadow
  public abstract ItemStack getItem();

  @Inject(at = @At("HEAD"), method = "tick")
  public void andromeda$tryPlant(CallbackInfo ci) {
    Entity entity = (Entity) (Object) this;
    ItemStack stack = this.getItem();
    BlockPos pos = entity.blockPosition();
    Level world = entity.level();

    if (world.isClientSide()) return;
    if (!(stack.getItem() instanceof BlockItem blockItem)
        || !(blockItem.getBlock() instanceof BushBlock)) return;

    if (entity.tickCount % MathUtil.nextInt(20, 101) != 0) return;
    var config = world.am$get(AutoPlanting.CONFIG);
    if (!config.available) return;
    if (!world.getFluidState(pos).isEmpty()) return;
    if (config.blacklistMode == stack.is(Main.ITEM_LIST)) return;

    blockItem.place(new BlockPlaceContext(
        world,
        null,
        null,
        stack,
        world.clip(new ClipContext(
            Vec3.atLowerCornerWithOffset(pos, 0.5, 0.5, 0.5),
            Vec3.atLowerCornerWithOffset(pos, 0.5, -0.5, 0.5),
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.ANY,
            entity))));
  }
}
