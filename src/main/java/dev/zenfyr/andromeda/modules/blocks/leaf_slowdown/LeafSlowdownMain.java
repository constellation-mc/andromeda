package dev.zenfyr.andromeda.modules.blocks.leaf_slowdown;

import dev.zenfyr.andromeda.common.Andromeda;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class LeafSlowdownMain {

  public static final Identifier LEAF_SLOWNESS_LOCATION = Andromeda.id("leaf_slowness");
  public static final AttributeModifier LEAF_SLOWNESS = new AttributeModifier(
      LEAF_SLOWNESS_LOCATION, -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

  public static void init() {}
}
