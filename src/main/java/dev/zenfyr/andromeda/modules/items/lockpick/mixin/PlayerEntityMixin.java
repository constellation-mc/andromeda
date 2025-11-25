package dev.zenfyr.andromeda.modules.items.lockpick.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.zenfyr.andromeda.modules.items.lockpick.LockpickItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerEntityMixin extends LivingEntity {

  protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
    super(entityType, world);
  }

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"),
      method = "interactOn")
  private InteractionResult andromeda$stopInteract(
      Entity entity, Player player, InteractionHand hand, Operation<InteractionResult> original) {
    if (getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof LockpickItem
        || getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof LockpickItem) {
      return InteractionResult.PASS;
    }
    return original.call(entity, player, hand);
  }
}
