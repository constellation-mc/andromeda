package me.melontini.andromeda.modules.items.minecart_block_picking.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.modules.items.minecart_block_picking.PlaceBehaviorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.item.MinecartItem$1")
abstract class ItemDispenserBehaviorMixin {

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;createMinecart(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/entity/vehicle/AbstractMinecart$Type;)Lnet/minecraft/world/entity/vehicle/AbstractMinecart;",
              shift = At.Shift.BEFORE),
      method = "execute",
      cancellable = true)
  public void andromeda$dispenseSilently(
      BlockSource pointer,
      ItemStack stack,
      CallbackInfoReturnable<ItemStack> cir,
      @Local(index = 5) double d,
      @Local(index = 7) double e,
      @Local(index = 9) double f,
      @Local(index = 14) double g,
      @Local BlockPos blockPos) {
    PlaceBehaviorHandler.getPlaceBehavior(stack.getItem()).ifPresent(b -> {
      if (!pointer.getLevel().isClientSide()) {
        AbstractMinecart entity = b.dispense(stack, pointer.getLevel(), d, e, f, g, blockPos);
        if (entity == null) return;

        pointer.getLevel().addFreshEntity(entity);
        stack.shrink(1);
      }
      cir.setReturnValue(stack);
    });
  }
}
