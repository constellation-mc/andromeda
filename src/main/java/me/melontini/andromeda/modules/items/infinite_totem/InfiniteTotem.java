package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.registries.Common;

public class InfiniteTotem implements Module {

    @Override
    public void onClient() {
        Common.bootstrap(Client.class);
    }

    @Override
    public void onMain() {
        Common.bootstrap(Content.class);
    }

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
