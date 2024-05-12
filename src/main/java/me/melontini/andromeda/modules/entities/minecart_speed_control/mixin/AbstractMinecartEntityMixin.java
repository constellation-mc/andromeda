package me.melontini.andromeda.modules.entities.minecart_speed_control.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.modules.entities.minecart_speed_control.MinecartSpeedControl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMinecartEntity.class)
abstract class AbstractMinecartEntityMixin extends Entity {

    public AbstractMinecartEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    private double andromeda$getMaxSpeed(double original) {
        if (!this.getWorld().isClient()) {
            var c = this.getWorld().am$get(MinecartSpeedControl.CONFIG);
            var supplier = ConstantLootContextAccessor.get(this);
            return c.available.asBoolean(supplier) ? original * c.modifier.asDouble(supplier) : original;
        }
        return original;
    }
}
