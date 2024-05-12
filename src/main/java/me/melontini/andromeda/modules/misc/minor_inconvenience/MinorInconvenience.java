package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "minor_inconvenience", category = "misc")
public final class MinorInconvenience extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    MinorInconvenience() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
