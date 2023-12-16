package me.melontini.andromeda.common.conflicts;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CommonRegistries {

    public static DefaultedRegistry<Item> items() {
        return Registries.ITEM;
    }

    public static DefaultedRegistry<Block> blocks() {
        return Registries.BLOCK;
    }

    public static DefaultedRegistry<EntityType<?>> entityTypes() {
        return Registries.ENTITY_TYPE;
    }

    public static Registry<ParticleType<?>> particleTypes() {
        return Registries.PARTICLE_TYPE;
    }
}
