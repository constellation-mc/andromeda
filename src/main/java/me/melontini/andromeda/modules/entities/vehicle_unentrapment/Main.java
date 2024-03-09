package me.melontini.andromeda.modules.entities.vehicle_unentrapment;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {

    public static TagKey<EntityType<?>> ESCAPE_VEHICLES_ON_HIT = TagKey.of(Registry.ENTITY_TYPE_KEY, id("escape_vehicles_on_hit"));
    public static TagKey<EntityType<?>> ESCAPABLE_VEHICLES = TagKey.of(Registry.ENTITY_TYPE_KEY, id("escapable_vehicles"));

    Main() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            World world = entity.getWorld();
            if (world.am$get(VehicleUnentrapment.class).enabled) {
                if (source.getAttacker() == null || entity instanceof PlayerEntity) return true;
                if (!entity.getType().isIn(ESCAPE_VEHICLES_ON_HIT)) return true;

                Entity vehicle = entity.getVehicle();
                if (vehicle == null || !vehicle.getType().isIn(ESCAPABLE_VEHICLES)) return true;
                entity.stopRiding();
            }
            return true;
        });
    }
}
