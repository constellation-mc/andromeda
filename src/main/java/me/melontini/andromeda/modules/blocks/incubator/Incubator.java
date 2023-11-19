package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.andromeda.registries.Common;

public class Incubator implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(Content.class, EggProcessingData.class);
    }

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
