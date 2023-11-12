package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.modules.mechanics.throwable_items.client.Client;
import me.melontini.andromeda.registries.Common;

public class ThrowableItems implements Module {

    @Override
    public void onClient() {
        Common.bootstrap(Client.class);
    }

    @Override
    public void onMain() {
        Common.bootstrap(Content.class);
    }

    @Override
    public boolean enabled() {
        return Config.get().throwableItems.enable;
    }
}
