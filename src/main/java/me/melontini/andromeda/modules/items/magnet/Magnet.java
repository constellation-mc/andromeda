package me.melontini.andromeda.modules.items.magnet;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;

import java.util.List;

@Unscoped
@ModuleInfo(name = "magnet", category = "items")
public final class Magnet extends Module {

    Magnet() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
