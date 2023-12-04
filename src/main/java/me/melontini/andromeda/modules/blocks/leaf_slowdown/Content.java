package me.melontini.andromeda.modules.blocks.leaf_slowdown;

import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.UUID;

import static me.melontini.andromeda.common.registries.Common.run;

public class Content {

    public static EntityAttributeModifier LEAF_SLOWNESS;

    public static void init() {
        Content.LEAF_SLOWNESS = run(() -> new EntityAttributeModifier(UUID.fromString("f72625eb-d4c4-4e1d-8e5c-1736b9bab349"), "Leaf Slowness", -0.3, EntityAttributeModifier.Operation.MULTIPLY_BASE), "leafSlowdown");
    }
}
