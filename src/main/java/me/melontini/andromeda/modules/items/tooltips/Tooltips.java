package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@FeatureEnvironment(Environment.CLIENT)
public class Tooltips implements Module<Tooltips.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean clock = true;

        @ConfigEntry.Gui.Tooltip
        public boolean compass = true;

        @ConfigEntry.Gui.Tooltip
        public boolean recoveryCompass = true;
    }
}
