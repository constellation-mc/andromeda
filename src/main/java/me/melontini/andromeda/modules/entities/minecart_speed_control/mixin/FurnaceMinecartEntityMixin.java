package me.melontini.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceMinecartEntity.class)
abstract class FurnaceMinecartEntityMixin {

    @Shadow public int fuel;

    @Unique
    private static final MinecartSpeedControl am$module = ModuleManager.quick(MinecartSpeedControl.class);

    @Inject(at = @At("HEAD"), method = "tick")
    private void andromeda$subtract(CallbackInfo ci) {
        if (!((AbstractMinecartEntity) (Object) this).getWorld().isClient()) {
            if (fuel > 0) {
                fuel -= ((AbstractMinecartEntity) (Object) this).getWorld().am$get(am$module).additionalFurnaceFuel;
            }
        }
    }

    @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    private double andromeda$getMaxSpeed(double original) {
        if (!((AbstractMinecartEntity) (Object) this).getWorld().isClient()) {
            return original * ((AbstractMinecartEntity) (Object) this).getWorld().am$get(am$module).furnaceModifier;
        }
        return original;
    }
}
