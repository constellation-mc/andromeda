package me.melontini.andromeda.modules.misc.damage_backport;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;

import java.util.List;

@Unscoped
@ModuleInfo(name = "damage_backport", category = "misc", environment = Environment.SERVER)
public class DamageBackport extends Module<Module.BaseConfig> {

    DamageBackport() {
        InitEvent.main(this).listen(() -> List.of(DamageCommand.class));
    }
}
