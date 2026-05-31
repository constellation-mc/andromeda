package dev.zenfyr.andromeda.modules.entities.boats.entities;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public abstract class BoatEntityWithBlock extends AbstractBoat {
  protected static final float PIby180 = (float) (Math.PI / 180.0);
  protected static final float PIby2 = (float) (Math.PI / 2);

  private final BoatRideHeightFactory rideHeight;

  public BoatEntityWithBlock(
      EntityType<? extends AbstractBoat> entityType,
      Level world,
      BoatRideHeightFactory rideHeight,
      Supplier<Item> supplier) {
    super(entityType, world, supplier);
    this.rideHeight = rideHeight;
  }

  @Override
  protected float getSinglePassengerXOffset() {
    return 0.15f;
  }

  @Override
  protected double rideHeight(EntityDimensions dimensions) {
    return this.rideHeight.applyAsDouble(dimensions);
  }

  @Override
  public int getMaxPassengers() {
    return 1;
  }
}
