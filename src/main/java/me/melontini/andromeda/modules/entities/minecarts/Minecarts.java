package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Unscoped
@ModuleInfo(name = "minecarts", category = "entities")
public class Minecarts extends Module<Minecarts.Config> {

    Minecarts() {
    }

    public static class Config extends BaseConfig {

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isAnvilMinecartOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isNoteBlockMinecartOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isJukeboxMinecartOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isSpawnerMinecartOn = false;
    }
}
