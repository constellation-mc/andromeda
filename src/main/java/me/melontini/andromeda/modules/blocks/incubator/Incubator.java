package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.modules.blocks.incubator.client.Client;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.andromeda.registries.Common;

public class Incubator implements Module {

    @Override
    public void onClient() {
        Common.bootstrap(Client.class);
    }

    @Override
    public void onMain() {
        Common.bootstrap(Content.class, EggProcessingData.class);
    }

    @Override
    public Class<?> configClass() {
        return Config.class;
    }
}
