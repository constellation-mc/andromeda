package me.melontini.andromeda.entity.vehicle.boats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BoatEntityWithBlock extends BoatEntity {
    protected final static float PIby180 = (float) (Math.PI / 180.0);
    protected final static float PIby2 = (float) (Math.PI / 2);
    public BoatEntityWithBlock(EntityType<? extends BoatEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            double g = ((this.isRemoved() ? 0.01F : this.getMountedHeightOffset()) + passenger.getHeightOffset());

            Vec3d vec3d = new Vec3d(0.3f, 0.0, 0.0).rotateY(-this.getYaw() * PIby180 - PIby2);
            passenger.setPosition(this.getX() + vec3d.x, this.getY() + g, this.getZ() + vec3d.z);
            passenger.setYaw(passenger.getYaw() + this.yawVelocity);
            passenger.setHeadYaw(passenger.getHeadYaw() + this.yawVelocity);
            this.copyEntityData(passenger);
        }
    }

    @Override
    public int getMaxPassengers() {
        return 1;
    }
}
