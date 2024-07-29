package me.melontini.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceMinecartEntity.class)
abstract class FurnaceMinecartEntityMixin extends AbstractMinecartEntity {

  @Shadow
  public int fuel;

  protected FurnaceMinecartEntityMixin(EntityType<?> entityType, World world) {
    super(entityType, world);
  }

  @Inject(at = @At("HEAD"), method = "tick")
  private void andromeda$subtract(CallbackInfo ci) {
    if (!this.getWorld().isClient()) {
      if (fuel > 0) {
        var c = this.getWorld().am$get(MinecartSpeedControl.CONFIG);
        var supplier = ConstantLootContextAccessor.get(this);
        if (c.available.asBoolean(supplier))
          fuel = Math.max(fuel - c.additionalFurnaceFuel.asInt(supplier), 0);
      }
    }
  }

  @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
  private double andromeda$getMaxSpeed(double original) {
    if (!this.getWorld().isClient()) {
      var c = this.getWorld().am$get(MinecartSpeedControl.CONFIG);
      var supplier = ConstantLootContextAccessor.get(this);
      return c.available.asBoolean(supplier)
          ? original * c.furnaceModifier.asDouble(supplier)
          : original;
    }
    return original;
  }
}
