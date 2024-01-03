package me.melontini.andromeda.modules.items.minecart_block_picking.mixin;

import me.melontini.andromeda.modules.items.minecart_block_picking.PlaceBehaviorHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/item/MinecartItem$1")
abstract class ItemDispenserBehaviorMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;create(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/vehicle/AbstractMinecartEntity$Type;)Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, method = "dispenseSilently", cancellable = true)
    public void andromeda$dispenseSilently(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, Direction direction, World world, double d, double e, double f, BlockPos blockPos, BlockState blockState, RailShape railShape, double g) {
        PlaceBehaviorHandler.getPlaceBehavior(stack.getItem()).ifPresent(b -> {
            if (!world.isClient()) {
                AbstractMinecartEntity entity = b.dispense(stack, world, d, e, f, g, blockPos);
                if (entity == null) return;

                world.spawnEntity(entity);
                stack.decrement(1);
            }
            cir.setReturnValue(stack);
        });
    }
}
