package dev.zenfyr.andromeda.modules.entities.boats.entities;

import java.util.function.ToDoubleFunction;
import net.minecraft.world.entity.EntityDimensions;

public interface BoatRideHeightFactory extends ToDoubleFunction<EntityDimensions> {
  @Override
  double applyAsDouble(EntityDimensions dimensions);
}
