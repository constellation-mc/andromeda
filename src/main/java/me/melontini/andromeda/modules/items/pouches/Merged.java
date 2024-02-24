package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.util.Debug;

public class Merged {

    Merged(Pouches module) {
        Main.testBlocks(module);

        if (Debug.Keys.PRINT_DEBUG_MESSAGES.isPresent()) {
            StringBuilder b = new StringBuilder();
            b.append("Viewable block entities:");
            Main.VIEWABLE_BLOCKS.forEach((blockEntityType, field) -> {
                b.append('\n').append(CommonRegistries.blockEntityTypes().getId(blockEntityType)).append(": ").append(field.getName());
            });
            module.logger().info(b);
        }
    }
}
