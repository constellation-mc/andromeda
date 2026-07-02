package dev.zenfyr.andromeda.modules.entities.zombie.clean_pickup.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.andromeda.modules.entities.zombie.clean_pickup.Pickup;
import dev.zenfyr.andromeda.modules.entities.zombie.clean_pickup.PickupTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
abstract class ZombieEntityMixin extends Monster {

  protected ZombieEntityMixin(EntityType<? extends Monster> entityType, Level world) {
    super(entityType, world);
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/monster/Monster;canHoldItem(Lnet/minecraft/world/item/ItemStack;)Z"),
      method = "canHoldItem")
  public boolean andromeda$canPickupItem(boolean original, ItemStack stack) {
    if (level().am$get(Pickup.CONFIG).available) {
      return original && (stack.is(PickupTag.ZOMBIES_PICKUP));
    }
    return original;
  }
}
