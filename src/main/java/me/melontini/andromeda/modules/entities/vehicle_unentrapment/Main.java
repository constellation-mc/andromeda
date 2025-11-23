package me.melontini.andromeda.modules.entities.vehicle_unentrapment;

import static me.melontini.andromeda.common.Andromeda.id;

import java.util.Objects;
import me.melontini.andromeda.common.util.LootContextBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;

public final class Main {

  public static final TagKey<EntityType<?>> ESCAPE_VEHICLES_ON_HIT =
      TagKey.create(Registries.ENTITY_TYPE, id("escape_vehicles_on_hit"));
  public static final TagKey<EntityType<?>> ESCAPABLE_VEHICLES =
      TagKey.create(Registries.ENTITY_TYPE, id("escapable_vehicles"));

  static void init() {
    ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
      Level world = entity.level();
      if (world
          .am$get(VehicleUnentrapment.CONFIG)
          .available
          .asBoolean(LootContextBuilder.entity(world, builder -> builder
              .origin(Objects.requireNonNullElse(source.getSourcePosition(), entity.position()))
              .thisEntity(entity)
              .sourceOrGeneric(source)
              .killer(source.getEntity())
              .directKiller(source.getDirectEntity())))) {
        if (source.getEntity() == null || entity instanceof Player) return true;
        if (!entity.getType().is(ESCAPE_VEHICLES_ON_HIT)) return true;

        Entity vehicle = entity.getVehicle();
        if (vehicle == null || !vehicle.getType().is(ESCAPABLE_VEHICLES)) return true;
        entity.stopRiding();
      }
      return true;
    });
  }
}
