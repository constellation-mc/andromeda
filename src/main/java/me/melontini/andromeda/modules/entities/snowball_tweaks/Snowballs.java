package me.melontini.andromeda.modules.entities.snowball_tweaks;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@FeatureEnvironment(Environment.SERVER)
public class Snowballs extends Module<Snowballs.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean freeze = true;

        @ConfigEntry.Gui.Tooltip
        public boolean extinguish = true;

        @ConfigEntry.Gui.Tooltip
        public boolean melt = true;

        @ConfigEntry.Gui.Tooltip
        public boolean layers = false;

        public boolean enableCooldown = true;

        @ConfigEntry.Gui.Tooltip
        public int cooldown = 10;
    }
}
