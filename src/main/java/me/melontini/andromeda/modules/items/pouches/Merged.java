package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.util.Debug;
import net.minecraft.registry.Registries;

public final class Merged {

    Merged(Pouches module) {
        Main.testBlocks(module);

        if (Debug.Keys.PRINT_DEBUG_MESSAGES.isPresent()) {
            StringBuilder b = new StringBuilder();
            b.append("Viewable block entities:");
            Main.VIEWABLE_VIEW.forEach((blockEntityType, field) -> {
                b.append('\n').append(Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType)).append(": ").append(field.getName());
            });
            module.logger().info(b);
        }
    }
}
