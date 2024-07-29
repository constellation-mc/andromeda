package me.melontini.andromeda.modules.entities.boats.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public abstract class BoatEntityWithBlock extends BoatEntity {
  protected static final float PIby180 = (float) (Math.PI / 180.0);
  protected static final float PIby2 = (float) (Math.PI / 2);

  public BoatEntityWithBlock(EntityType<? extends BoatEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected float getPassengerHorizontalOffset() {
    return 0.15f;
  }

  @Override
  public int getMaxPassengers() {
    return 1;
  }
}
