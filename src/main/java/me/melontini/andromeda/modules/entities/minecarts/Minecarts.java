package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.modules.entities.minecarts.client.Client;
import me.melontini.andromeda.registries.Common;

public class Minecarts implements Module {

    @Override
    public void onClient() {
        Common.bootstrap(Client.class);
    }

    @Override
    public void onMain() {
        Common.bootstrap(MinecartItems.class);
    }

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;   }
}
