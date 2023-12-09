package me.melontini.andromeda.modules.mechanics.dragon_fight;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "dragon_fight", category = "mechanics", environment = Environment.SERVER)
public class DragonFight extends Module<DragonFight.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public boolean respawnCrystals = true;

        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public boolean scaleHealthByMaxPlayers = false;

        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public boolean shorterCrystalTrackRange = true;

        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public boolean shorterSpikes = false;
    }
}
