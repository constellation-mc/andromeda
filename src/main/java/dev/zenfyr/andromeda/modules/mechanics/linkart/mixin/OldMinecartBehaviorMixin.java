package dev.zenfyr.andromeda.modules.mechanics.linkart.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkableMinecart;
import dev.zenfyr.andromeda.modules.mechanics.linkart.Main;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
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
                  "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
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

  @WrapOperation(
      method = "moveAlongTrack",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
  private double linkart$skipVelocityClamping(
      double value, double min, double max, Operation<Double> original) {
    if (((LinkableMinecart) this.minecart).linkart$getFollowing() != null) {
      AbstractMinecart following = ((LinkableMinecart) this.minecart).linkart$getFollowing();
      while (((LinkableMinecart) following).linkart$getFollowing() != null) {
        following = ((LinkableMinecart) following).linkart$getFollowing();
      }
      double parent = following.getMaxSpeed((ServerLevel) following.level());
      return Mth.clamp(value, -parent, parent);
    }
    return original.call(value, min, max);
  }
}
