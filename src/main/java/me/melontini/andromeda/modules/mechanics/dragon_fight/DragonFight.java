package me.melontini.andromeda.modules.mechanics.dragon_fight;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "dragon_fight", category = "mechanics", environment = Environment.SERVER)
public class DragonFight extends Module<DragonFight.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("dragonFight")) {
            JsonObject dragonFight = config.getAsJsonObject("dragonFight");

            JsonOps.ifPresent(dragonFight, "respawnCrystals", e -> this.config().respawnCrystals = e.getAsBoolean());
            JsonOps.ifPresent(dragonFight, "scaleHealthByMaxPlayers", e -> this.config().scaleHealthByMaxPlayers = e.getAsBoolean());
            JsonOps.ifPresent(dragonFight, "shorterCrystalTrackRange", e -> this.config().shorterCrystalTrackRange = e.getAsBoolean());
            JsonOps.ifPresent(dragonFight, "shorterSpikes", e -> this.config().shorterSpikes = e.getAsBoolean());
        }
    }

    @Override
    public void onMain() {
        Common.bootstrap(this, EnderDragonManager.class);
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
