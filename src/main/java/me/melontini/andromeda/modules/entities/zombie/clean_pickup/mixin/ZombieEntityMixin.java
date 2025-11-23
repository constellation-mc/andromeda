package me.melontini.andromeda.modules.entities.zombie.clean_pickup.mixin;

import static java.util.Objects.requireNonNull;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.modules.entities.zombie.clean_pickup.Pickup;
import me.melontini.andromeda.modules.entities.zombie.clean_pickup.PickupTag;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
    if (level.am$get(Pickup.CONFIG).available.asBoolean(ConstantLootContextAccessor.get(this))) {
      return original
          && (stack.is(PickupTag.ZOMBIES_PICKUP)
              || ModuleManager.get()
                  .get(ThrowableItems.class)
                  .map(m -> handleThrowableItems(m, level, stack))
                  .orElse(false));
    }
    return original;
  }

  @Unique private boolean handleThrowableItems(ThrowableItems m, Level world, ItemStack stack) {
    return world.am$get(ThrowableItems.CONFIG).canZombiesThrowItems
        && requireNonNull(world.getServer()).dm$getReloader(RELOADER).hasBehaviors(stack);
  }
}
