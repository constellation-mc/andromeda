package me.melontini.andromeda.mixin.entities.boat_impl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.entity.vehicle.boats.FurnaceBoatEntity;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatEntity.class)
@MixinRelatedConfigOption("newBoats.isFurnaceBoatOn")
public abstract class BoatEntityMixin extends Entity {

    @Shadow
    private BoatEntity.Location location;

    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;setVelocity(DDD)V", ordinal = 0), method = "updateVelocity")
    private void andromeda$furnaceBoatVelocity(BoatEntity boat, double x, double y, double z, Operation<Void> operation) {
        if (((BoatEntity)(Object)this) instanceof FurnaceBoatEntity furnaceBoat) {
            if (furnaceBoat.getFuel() > 0) {
                Vec3d rotationVec = this.getRotationVec(1.0F);
                if (this.location == BoatEntity.Location.ON_LAND) operation.call(boat, rotationVec.getX() * 0.1, y, rotationVec.getZ() * 0.1);
                else operation.call(boat, rotationVec.getX() * 0.4, y, rotationVec.getZ() * 0.4);
                return;
            }
        }
        operation.call(boat, x, y, z);
    }

}
