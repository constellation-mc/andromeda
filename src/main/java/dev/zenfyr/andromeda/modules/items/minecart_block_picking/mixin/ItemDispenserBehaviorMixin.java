package dev.zenfyr.andromeda.modules.items.minecart_block_picking.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.items.minecart_block_picking.PlaceBehaviorHandler;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.MinecartDispenseItemBehavior;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartDispenseItemBehavior.class)
abstract class ItemDispenserBehaviorMixin {

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;createMinecart(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;",
              shift = At.Shift.BEFORE),
      method = "execute",
      cancellable = true)
  public void andromeda$dispenseSilently(
      BlockSource source,
      ItemStack stack,
      CallbackInfoReturnable<ItemStack> cir,
      @Local(index = 6) double d,
      @Local(index = 8) double e,
      @Local(index = 10) double f,
      @Local(index = 14) double g) {
    PlaceBehaviorHandler.getPlaceBehavior(stack.getItem()).ifPresent(b -> {
      if (!source.level().isClientSide()) {
        AbstractMinecart entity = b.dispense(stack, source.level(), d, e, f, g, source.pos());
        if (entity == null) return;

        source.level().addFreshEntity(entity);
        stack.shrink(1);
      }
      cir.setReturnValue(stack);
    });
  }
}
