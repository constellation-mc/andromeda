package me.melontini.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceMinecartEntity.class)
abstract class FurnaceMinecartEntityMixin {

    @Shadow public int fuel;

    @Inject(at = @At("HEAD"), method = "tick")
    private void andromeda$subtract(CallbackInfo ci) {
        if (!((AbstractMinecartEntity) (Object) this).getWorld().isClient()) {
            if (fuel > 0) {
                var c = ((AbstractMinecartEntity) (Object) this).getWorld().am$get(MinecartSpeedControl.CONFIG);
                if (c.available) fuel = Math.max(fuel - c.additionalFurnaceFuel, 0);
            }
        }
    }

    @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    private double andromeda$getMaxSpeed(double original) {
        if (!((AbstractMinecartEntity) (Object) this).getWorld().isClient()) {
            var c = ((AbstractMinecartEntity) (Object) this).getWorld().am$get(MinecartSpeedControl.CONFIG);
            return c.available ? original * c.furnaceModifier : original;
        }
        return original;
    }
}
