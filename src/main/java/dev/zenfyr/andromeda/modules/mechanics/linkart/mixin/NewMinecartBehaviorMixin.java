package dev.zenfyr.andromeda.modules.mechanics.linkart.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkableMinecart;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkartMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {

  protected NewMinecartBehaviorMixin(AbstractMinecart minecart) {
    super(minecart);
  }

  // Ensure the train doesn't break apart (especially if other minecart mods increase speed)
  @ModifyArg(
      method = "stepAlongTrack",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
              ordinal = 0))
  private Vec3 modifiedMovement(Vec3 movement) {
    if (((LinkableMinecart) this.minecart).linkart$lastMovementLength() < movement.length()) {
      final double targetMovementLength = movement.length();

      // Limit the movement length
      movement = movement.scale(
          LinkartMain.limitMovementLength(((LinkableMinecart) this.minecart), targetMovementLength)
              / targetMovementLength);
    }

    ((LinkableMinecart) this.minecart).linkart$lastMovementLength(movement.length());
    return movement;
  }

  @Inject(
      method = "getMaxSpeed(Lnet/minecraft/server/level/ServerLevel;)D",
      at = @At("HEAD"),
      cancellable = true)
  private void linkart$overrideMaxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
    if (((LinkableMinecart) this.minecart).linkart$getFollowing() != null) {
      AbstractMinecart following = ((LinkableMinecart) this.minecart).linkart$getFollowing();
      while (((LinkableMinecart) following).linkart$getFollowing() != null) {
        following = ((LinkableMinecart) following).linkart$getFollowing();
      }

      cir.setReturnValue(following.getMaxSpeed(level));
    }
  }

  @ModifyExpressionValue(
      method = "calculateTrackSpeed",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;getMaxSpeed(Lnet/minecraft/server/level/ServerLevel;)D"))
  private double linkart$skipVelocityClamping(double original) {
    if (((LinkableMinecart) this.minecart).linkart$getFollowing() != null) {
      AbstractMinecart following = ((LinkableMinecart) this.minecart).linkart$getFollowing();
      while (((LinkableMinecart) following).linkart$getFollowing() != null) {
        following = ((LinkableMinecart) following).linkart$getFollowing();
      }
      return following.getMaxSpeed((ServerLevel) following.level());
    }
    return original;
  }
}
