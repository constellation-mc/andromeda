package me.melontini.andromeda.modules.mechanics.dragon_fight;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Unscoped
@ModuleInfo(name = "dragon_fight", category = "mechanics", environment = Environment.SERVER)
public class DragonFight extends Module<DragonFight.Config> {

    DragonFight() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    public static class Config extends BaseConfig {

        @ConfigEntry.Category("mechanics")
        public boolean respawnCrystals = true;

        @ConfigEntry.Category("mechanics")
        public boolean scaleHealthByMaxPlayers = false;

        @ConfigEntry.Category("mechanics")
        public boolean shorterCrystalTrackRange = true;

        @ConfigEntry.Category("mechanics")
        public boolean shorterSpikes = false;
    }
}
