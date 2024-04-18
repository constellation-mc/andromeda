package me.melontini.andromeda.modules.items.minecart_block_picking.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.modules.items.minecart_block_picking.PlaceBehaviorHandler;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/item/MinecartItem$1")
abstract class ItemDispenserBehaviorMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;create(Lnet/minecraft/server/world/ServerWorld;DDDLnet/minecraft/entity/vehicle/AbstractMinecartEntity$Type;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;", shift = At.Shift.BEFORE), method = "dispenseSilently", cancellable = true)
    public void andromeda$dispenseSilently(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, @Local(index = 6) double d, @Local(index = 8) double e, @Local(index = 10) double f, @Local(index = 15) double g, @Local BlockPos blockPos) {
        PlaceBehaviorHandler.getPlaceBehavior(stack.getItem()).ifPresent(b -> {
            if (!pointer.world().isClient()) {
                AbstractMinecartEntity entity = b.dispense(stack, pointer.world(), d, e, f, g, blockPos);
                if (entity == null) return;

                pointer.world().spawnEntity(entity);
                stack.decrement(1);
            }
            cir.setReturnValue(stack);
        });
    }
}
