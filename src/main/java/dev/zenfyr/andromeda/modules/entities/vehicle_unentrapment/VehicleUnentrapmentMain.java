package dev.zenfyr.andromeda.modules.entities.vehicle_unentrapment;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class VehicleUnentrapmentMain {

  public static final TagKey<EntityType<?>> ESCAPE_VEHICLES_ON_HIT =
      TagKey.create(Registries.ENTITY_TYPE, id("escape_vehicles_on_hit"));
  public static final TagKey<EntityType<?>> ESCAPABLE_VEHICLES =
      TagKey.create(Registries.ENTITY_TYPE, id("escapable_vehicles"));

  static void init() {
    ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
      Level level = entity.level();
      if (level.am$get(VehicleUnentrapment.CONFIG).available) {
        if (source.getEntity() == null || entity instanceof Player) return true;
        if (!entity.typeHolder().is(ESCAPE_VEHICLES_ON_HIT)) return true;

        Entity vehicle = entity.getVehicle();
        if (vehicle == null || !vehicle.typeHolder().is(ESCAPABLE_VEHICLES)) return true;
        entity.stopRiding();
      }
      return true;
    });
  }
}
