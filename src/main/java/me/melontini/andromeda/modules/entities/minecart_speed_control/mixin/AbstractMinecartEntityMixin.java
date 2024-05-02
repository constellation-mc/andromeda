package me.melontini.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMinecartEntity.class)
abstract class AbstractMinecartEntityMixin {

    @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    private double andromeda$getMaxSpeed(double original) {
        if (!((AbstractMinecartEntity) (Object) this).getWorld().isClient()) {
            var c = ((AbstractMinecartEntity) (Object) this).getWorld().am$get(MinecartSpeedControl.CONFIG);
            return c.available ? original * c.modifier : original;
        }
        return original;
    }
}
