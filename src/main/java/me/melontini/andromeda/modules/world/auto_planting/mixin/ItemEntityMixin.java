package me.melontini.andromeda.modules.world.auto_planting.mixin;


import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.world.auto_planting.AutoPlanting;
import me.melontini.dark_matter.api.base.util.MathUtil;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {

    @Shadow
    public abstract ItemStack getStack();

    @Inject(at = @At("HEAD"), method = "tick")
    public void andromeda$tryPlant(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        ItemStack stack = this.getStack();
        BlockPos pos = entity.getBlockPos();
        World world = entity.getWorld();

        if (world.isClient()) return;
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof PlantBlock)) return;

        if (entity.age % MathUtil.nextInt(20, 101) != 0) return;
        var config = world.am$get(AutoPlanting.CONFIG);
        if (!config.available.asBoolean(LootContextUtil.fishing(world, entity.getPos(), stack, entity))) return;
        if (!world.getFluidState(pos).isEmpty()) return;
        if (config.blacklistMode == config.idList.contains(stack.getItem()))
            return;

        blockItem.place(new ItemPlacementContext(world, null, null, stack,
                world.raycast(new RaycastContext(
                        Vec3d.add(pos, 0.5, 0.5, 0.5),
                        Vec3d.add(pos, 0.5, -0.5, 0.5),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.ANY,
                        entity)
                )));
    }
}
