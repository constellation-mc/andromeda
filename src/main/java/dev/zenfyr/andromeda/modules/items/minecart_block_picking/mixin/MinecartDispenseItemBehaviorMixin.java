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
abstract class MinecartDispenseItemBehaviorMixin {

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
      ItemStack dispensed,
      CallbackInfoReturnable<ItemStack> cir,
      @Local(name = "spawnX") double spawnX,
      @Local(name = "spawnY") double spawnY,
      @Local(name = "spawnZ") double spawnZ,
      @Local(name = "yOffset") double yOffset) {
    PlaceBehaviorHandler.getPlaceBehavior(dispensed.getItem()).ifPresent(b -> {
      if (!source.level().isClientSide()) {
        AbstractMinecart entity =
            b.dispense(dispensed, source.level(), spawnX, spawnY, spawnZ, yOffset, source.pos());
        if (entity == null) return;

        source.level().addFreshEntity(entity);
        dispensed.shrink(1);
      }
      cir.setReturnValue(dispensed);
    });
  }
}
