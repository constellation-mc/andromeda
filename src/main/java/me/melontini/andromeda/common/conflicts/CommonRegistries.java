package me.melontini.andromeda.common.conflicts;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public class CommonRegistries {

    public static DefaultedRegistry<Item> items() {
        return Registry.ITEM;
    }

    public static DefaultedRegistry<Block> blocks() {
        return Registry.BLOCK;
    }

    public static DefaultedRegistry<EntityType<?>> entityTypes() {
        return Registry.ENTITY_TYPE;
    }

    public static Registry<BlockEntityType<?>> blockEntityTypes() {
        return Registry.BLOCK_ENTITY_TYPE;
    }

    public static Registry<ParticleType<?>> particleTypes() {
        return Registry.PARTICLE_TYPE;
    }

    public static Registry<StatusEffect> statusEffects() {
        return Registry.STATUS_EFFECT;
    }
}
