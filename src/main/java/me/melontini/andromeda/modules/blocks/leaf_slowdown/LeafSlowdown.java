package me.melontini.andromeda.modules.blocks.leaf_slowdown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@Deprecated
@ModuleInfo(name = "leaf_slowdown", category = "blocks", environment = Environment.SERVER)
public class LeafSlowdown extends Module {

    LeafSlowdown() {
        this.defineConfig(ConfigState.GAME, ConfigDefinition.GAME);
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
