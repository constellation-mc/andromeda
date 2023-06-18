package me.melontini.andromeda.mixin.entities.boat_impl;

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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BoatEntity.class)
@MixinRelatedConfigOption("newBoats.isFurnaceBoatOn")
public abstract class BoatEntityMixin extends Entity {
    @Shadow
    public float yawVelocity;
    @Shadow
    private float velocityDecay;
    @Shadow
    private BoatEntity.Location location;

    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;", ordinal = 1, shift = At.Shift.BEFORE), method = "updateVelocity", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void andromeda$furnaceBoatVelocity(CallbackInfo ci, double e, double f) {
        BoatEntity boat = (BoatEntity) (Object) this;
        if (boat instanceof FurnaceBoatEntity furnaceBoat) {
            Vec3d vec3d = this.getVelocity();
            if (furnaceBoat.getFuel() > 0 && this.location != BoatEntity.Location.ON_LAND) {
                this.setVelocity(this.getRotationVec(1.0F).getX() * 0.4, vec3d.y + e, this.getRotationVec(1.0F).getZ() * 0.4);
                this.yawVelocity *= this.velocityDecay;
                if (f > 0.0) {
                    Vec3d vec3d2 = this.getVelocity();
                    this.setVelocity(this.getRotationVec(1.0F).getX() * 0.4, (vec3d2.y + f * 0.06) * 0.75, this.getRotationVec(1.0F).getZ() * 0.4);
                }
                ci.cancel();
            } else if (furnaceBoat.getFuel() > 0 && this.location == BoatEntity.Location.ON_LAND) {
                this.setVelocity(this.getRotationVec(1.0F).getX() * 0.1, vec3d.y + e, this.getRotationVec(1.0F).getZ() * 0.1);
                this.yawVelocity *= this.velocityDecay;
                if (f > 0.0) {
                    Vec3d vec3d2 = this.getVelocity();
                    this.setVelocity(this.getRotationVec(1.0F).getX() * 0.1, (vec3d2.y + f * 0.06) * 0.75, this.getRotationVec(1.0F).getZ() * 0.1);
                }
                ci.cancel();
            }
        }
    }
}
