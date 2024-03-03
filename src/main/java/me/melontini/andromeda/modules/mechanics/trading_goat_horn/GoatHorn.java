package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "trading_goat_horn", category = "mechanics", environment = Environment.SERVER)
public class GoatHorn extends Module<Module.BaseConfig> {

    GoatHorn() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
