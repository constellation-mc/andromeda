package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.misc.unknown.client.Client;

import java.util.List;

@Unscoped
@ModuleInfo(name = "unknown", category = "misc")
public class Unknown extends Module<Module.BaseConfig> {

    Unknown() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }
}
