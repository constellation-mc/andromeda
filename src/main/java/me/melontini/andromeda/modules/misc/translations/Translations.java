package me.melontini.andromeda.modules.misc.translations;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.modules.misc.translations.client.Client;

import java.util.List;

@ModuleInfo(name = "translations", category = "misc", environment = Environment.CLIENT)
public class Translations extends Module<Module.BaseConfig> {

    Translations() {
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }
}
