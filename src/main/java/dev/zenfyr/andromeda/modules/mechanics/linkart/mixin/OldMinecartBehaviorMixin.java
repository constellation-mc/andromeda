package dev.zenfyr.andromeda.modules.mechanics.linkart.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkableMinecart;
import dev.zenfyr.andromeda.modules.mechanics.linkart.Main;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {

  protected OldMinecartBehaviorMixin(AbstractMinecart minecart) {
    super(minecart);
  }

  // Ensure the train doesn't break apart (especially if other minecart mods increase speed)
  @ModifyArg(
      method = "moveAlongTrack",
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
          Main.limitMovementLength(((LinkableMinecart) this.minecart), targetMovementLength)
              / targetMovementLength);
    }

    ((LinkableMinecart) this.minecart).linkart$lastMovementLength(movement.length());
    return movement;
  }

  @ModifyExpressionValue(
      method = "moveAlongTrack",
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
