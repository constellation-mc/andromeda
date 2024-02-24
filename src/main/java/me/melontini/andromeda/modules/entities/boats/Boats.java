package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Unscoped
@ModuleInfo(name = "boats", category = "entities")
public class Boats extends Module<Boats.Config> {

    Boats() {
    }

    public static class Config extends BaseConfig {

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isFurnaceBoatOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isTNTBoatOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isJukeboxBoatOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isHopperBoatOn = false;
    }
}
