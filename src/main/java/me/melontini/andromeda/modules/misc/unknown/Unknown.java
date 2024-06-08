package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "unknown", category = "misc")
public final class Unknown extends Module {

    Unknown() {
        InitEvent.main(this).listen(() -> RoseOfTheValley::init);
        InitEvent.client(this).listen(() -> RoseOfTheValley::onClient);
    }
}
