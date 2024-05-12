package me.melontini.andromeda.modules.entities.vehicle_unentrapment;

import me.melontini.andromeda.common.util.LootContextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;

import java.util.Objects;

import static me.melontini.andromeda.common.Andromeda.id;

public final class Main {

    public static final TagKey<EntityType<?>> ESCAPE_VEHICLES_ON_HIT = TagKey.of(RegistryKeys.ENTITY_TYPE, id("escape_vehicles_on_hit"));
    public static final TagKey<EntityType<?>> ESCAPABLE_VEHICLES = TagKey.of(RegistryKeys.ENTITY_TYPE, id("escapable_vehicles"));

    Main() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            World world = entity.getWorld();
            if (world.am$get(VehicleUnentrapment.CONFIG).available.asBoolean(LootContextUtil.entity(world,
                    Objects.requireNonNullElse(source.getPosition(), entity.getPos()),
                    entity, source, source.getAttacker(), source.getSource()))) {
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
