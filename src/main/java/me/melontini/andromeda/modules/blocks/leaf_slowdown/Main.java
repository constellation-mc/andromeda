package me.melontini.andromeda.modules.blocks.leaf_slowdown;

import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.UUID;

public class Main {

    public static EntityAttributeModifier LEAF_SLOWNESS;

    Main() {
        LEAF_SLOWNESS = new EntityAttributeModifier(UUID.fromString("f72625eb-d4c4-4e1d-8e5c-1736b9bab349"), "Leaf Slowness", -0.3, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }
}
