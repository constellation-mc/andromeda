package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "minor_inconvenience", category = "misc")
public class MinorInconvenience extends Module<Module.BaseConfig> {

    MinorInconvenience() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
