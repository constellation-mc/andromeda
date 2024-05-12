package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "trading_goat_horn", category = "mechanics", environment = Environment.SERVER)
public final class GoatHorn extends Module {

    public static final ConfigDefinition<Module.GameConfig> CONFIG = new ConfigDefinition<>(() -> Module.GameConfig.class);

    GoatHorn() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
