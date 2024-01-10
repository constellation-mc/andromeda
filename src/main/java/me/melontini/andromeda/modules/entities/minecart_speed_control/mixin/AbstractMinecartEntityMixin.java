package me.melontini.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMinecartEntity.class)
abstract class AbstractMinecartEntityMixin {

    private static final MinecartSpeedControl am$module = ModuleManager.quick(MinecartSpeedControl.class);

    @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    private double andromeda$getMaxSpeed(double original) {
        return original * ((AbstractMinecartEntity) (Object) this).getWorld().am$get(am$module).modifier;
    }
}
