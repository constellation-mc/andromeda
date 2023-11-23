package me.melontini.andromeda.modules.entities.slimes;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@FeatureEnvironment(Environment.SERVER)
public class Slimes extends Module<Slimes.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean flee = true;

        @ConfigEntry.Gui.Tooltip
        public boolean merge = true;

        @ConfigEntry.Gui.Tooltip
        public int maxMerge = 4;

        @ConfigEntry.Gui.Tooltip
        public boolean slowness = false;
    }
}
