package dev.zenfyr.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zenfyr.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMinecart.class)
abstract class AbstractMinecartEntityMixin extends Entity {

  public AbstractMinecartEntityMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
  private double andromeda$getMaxSpeed(double original) {
    if (!this.level().isClientSide()) {
      var c = this.level().am$get(MinecartSpeedControl.CONFIG);
      return c.available ? original * c.modifier : original;
    }
    return original;
  }
}
