package me.melontini.andromeda.modules.entities.boats;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.base.events.LegacyConfigEvent;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Unscoped
@ModuleInfo(name = "boats", category = "entities")
public class Boats extends Module<Boats.Config> {

    Boats() {
        LegacyConfigEvent.BUS.listen(config -> {
            if (config.has("newBoats")) {
                JsonObject newBoats = config.getAsJsonObject("newBoats");

                JsonOps.ifPresent(newBoats, "isFurnaceBoatOn", e -> this.config().isFurnaceBoatOn = e.getAsBoolean());
                JsonOps.ifPresent(newBoats, "isTNTBoatOn", e -> this.config().isTNTBoatOn = e.getAsBoolean());
                JsonOps.ifPresent(newBoats, "isJukeboxBoatOn", e -> this.config().isJukeboxBoatOn = e.getAsBoolean());
                JsonOps.ifPresent(newBoats, "isHopperBoatOn", e -> this.config().isHopperBoatOn = e.getAsBoolean());
            }
        });
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
