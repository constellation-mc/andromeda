package me.melontini.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartFurnace.class)
abstract class FurnaceMinecartEntityMixin extends AbstractMinecart {

  @Shadow
  public int fuel;

  protected FurnaceMinecartEntityMixin(EntityType<?> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(at = @At("HEAD"), method = "tick")
  private void andromeda$subtract(CallbackInfo ci) {
    if (!this.level().isClientSide()) {
      if (fuel > 0) {
        var c = this.level().am$get(MinecartSpeedControl.CONFIG);
        if (c.active) fuel = Math.max(fuel - c.additionalFurnaceFuel, 0);
      }
    }
  }

  @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
  private double andromeda$getMaxSpeed(double original) {
    if (!this.level().isClientSide()) {
      var c = this.level().am$get(MinecartSpeedControl.CONFIG);
      return c.active ? original * c.furnaceModifier : original;
    }
    return original;
  }
}
