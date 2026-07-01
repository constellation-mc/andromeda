package dev.zenfyr.andromeda.modules.mechanics.linkart.mixin;

import dev.zenfyr.andromeda.modules.mechanics.linkart.CartOperation;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkableMinecart;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkartMain;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

  protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
    super(entityType, world);
  }

  @Unique private CartOperation operation;

  @Inject(at = @At("HEAD"), method = "interactOn", cancellable = true)
  void onInteract(
      Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    if (entity instanceof AbstractMinecart minecart) {
      if (level().isClientSide()) return;

      Player player = (Player) (Object) this;
      ItemStack stack = player.getItemInHand(hand);

      if (!stack.is(LinkartMain.LINKERS)) return;

      if (this.operation != null) {
        if (this.operation.minecart() != null
            && this.operation.minecart() != minecart
            && minecart.isAlive()
            && this.operation.minecart().isAlive()) {
          var result = this.operation.type().perform(minecart, this.operation, stack);
          if (result.consumesAction() && !player.isCreative()) stack.shrink(1);
          finishOperation(cir, minecart, result);
        } else {
          finishOperation(cir, minecart, InteractionResult.FAIL);
        }
        this.operation = null;
      } else if (((LinkableMinecart) minecart).linkart$getFollower() != null) {
        this.operation = new CartOperation(CartOperation.Type.UNLINKING, minecart);
        finishOperation(cir, minecart, InteractionResult.SUCCESS);
      } else {
        this.operation = new CartOperation(CartOperation.Type.LINKING, minecart);
        finishOperation(cir, minecart, InteractionResult.SUCCESS);
      }
    }
  }

  @Unique private void finishOperation(
      CallbackInfoReturnable<InteractionResult> cir,
      AbstractMinecart minecart,
      InteractionResult result) {
    if (result.consumesAction()) {
      ((ServerLevel) minecart.level())
          .sendParticles(
              ParticleTypes.HAPPY_VILLAGER,
              minecart.getX(),
              minecart.getY() + 0.2,
              minecart.getZ(),
              10,
              0.5,
              0.5,
              0.5,
              0.5);
    } else {
      ((ServerLevel) minecart.level())
          .sendParticles(
              ParticleTypes.ANGRY_VILLAGER,
              minecart.getX(),
              minecart.getY() + 0.2,
              minecart.getZ(),
              10,
              0.5,
              0.5,
              0.5,
              0.5);
    }
    cir.setReturnValue(result);
  }
}
