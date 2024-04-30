package me.melontini.andromeda.modules.mechanics.dragon_fight;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;

import java.util.List;

@Unscoped
@ModuleInfo(name = "dragon_fight", category = "mechanics", environment = Environment.SERVER)
public class DragonFight extends Module<DragonFight.Config> {

    DragonFight() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    @ToString
    public static class Config extends BaseConfig {
        public boolean respawnCrystals = true;
        public boolean scaleHealthByMaxPlayers = false;
        public boolean shorterCrystalTrackRange = true;
        public boolean shorterSpikes = false;
    }
}
