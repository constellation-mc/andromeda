package me.melontini.andromeda.mixin.world.self_planting;


import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemEntity.class)
@MixinRelatedConfigOption("autoPlanting.enabled")
public abstract class ItemEntityMixin {
    @Unique
    private final Random andromeda$random = new Random();

    @Shadow
    public abstract ItemStack getStack();

    @Inject(at = @At("HEAD"), method = "tick")
    public void andromeda$tryPlant(CallbackInfo ci) {
        if (!Config.get().autoPlanting.enabled) return;

        Entity entity = (Entity) (Object) this;
        ItemStack stack = this.getStack();
        BlockPos pos = entity.getBlockPos();
        World world = entity.getWorld();
        if (!world.isClient()) {
            if (entity.age % andromeda$random.nextInt(20, 101) == 0) {
                if (stack.getItem() instanceof BlockItem) {
                    if (((BlockItem) stack.getItem()).getBlock() instanceof PlantBlock) {
                        if (world.getFluidState(pos).isEmpty()) {
                            if (Config.get().autoPlanting.blacklistMode == Config.get().autoPlanting.idList.contains(Registry.ITEM.getId(stack.getItem()).toString())) return;

                            ((BlockItem) stack.getItem()).place(new ItemPlacementContext(world, null, null, stack, world.raycast(
                                    new RaycastContext(
                                            new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                                            new Vec3d(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5),
                                            RaycastContext.ShapeType.COLLIDER,
                                            RaycastContext.FluidHandling.ANY,
                                            entity
                                    )
                            )));
                        }
                    }
                }
            }
        }
    }
}
