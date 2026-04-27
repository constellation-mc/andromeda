package dev.zenfyr.andromeda.modules.entities.boats.entities;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public abstract class BoatEntityWithBlock extends Boat {
  protected static final float PIby180 = (float) (Math.PI / 180.0);
  protected static final float PIby2 = (float) (Math.PI / 2);

  public BoatEntityWithBlock(
      EntityType<? extends Boat> entityType, Level world, Supplier<Item> supplier) {
    super(entityType, world, supplier);
  }

  @Override
  protected float getSinglePassengerXOffset() {
    return 0.15f;
  }

  @Override
  public int getMaxPassengers() {
    return 1;
  }
}
