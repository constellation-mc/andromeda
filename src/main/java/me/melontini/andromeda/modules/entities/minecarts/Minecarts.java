package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.registries.Common;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class Minecarts implements Module<Minecarts.Config> {

    @Override
    public void onMain() {
        Common.bootstrap(MinecartItems.class);
    }

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.BOTH)
        public boolean isAnvilMinecartOn = false;

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.BOTH)
        public boolean isNoteBlockMinecartOn = false;

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.BOTH)
        public boolean isJukeboxMinecartOn = false;
    }
}
