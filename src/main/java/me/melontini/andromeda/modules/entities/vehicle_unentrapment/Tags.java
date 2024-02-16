package me.melontini.andromeda.modules.entities.vehicle_unentrapment;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import static me.melontini.andromeda.common.registries.Common.id;

public class Tags {
    public static TagKey<EntityType<?>> ESCAPE_VEHICLES_ON_HIT = TagKey.of(RegistryKeys.ENTITY_TYPE, id("escape_vehicles_on_hit"));
    public static TagKey<EntityType<?>> ESCAPABLE_VEHICLES = TagKey.of(RegistryKeys.ENTITY_TYPE, id("escapable_vehicles"));
}
