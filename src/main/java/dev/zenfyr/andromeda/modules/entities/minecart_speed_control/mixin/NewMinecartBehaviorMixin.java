package dev.zenfyr.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zenfyr.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {

  protected NewMinecartBehaviorMixin(AbstractMinecart minecart) {
    super(minecart);
  }

  @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
  private double andromeda$getMaxSpeed(double original) {
    if (!this.level().isClientSide()) {
      var c = this.level().am$get(MinecartSpeedControl.CONFIG);
      return c.available
          ? original * (this.minecart instanceof MinecartFurnace ? c.furnaceModifier : c.modifier)
          : original;
    }
    return original;
  }
}
