package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.registries.Common;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip
@ModuleInfo(name = "boats", category = "entities")
public class Boats extends Module<Boats.Config> {

    @Override
    public void onMain() {
        Common.bootstrap(BoatItems.class, BoatEntities.class);
    }

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.BOTH)
        public boolean isFurnaceBoatOn = false;

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.BOTH)
        public boolean isTNTBoatOn = false;

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.BOTH)
        public boolean isJukeboxBoatOn = false;

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.BOTH)
        public boolean isHopperBoatOn = false;
    }
}
