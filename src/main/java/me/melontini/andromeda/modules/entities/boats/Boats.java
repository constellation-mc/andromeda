package me.melontini.andromeda.modules.entities.boats;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip
@ModuleInfo(name = "boats", category = "entities")
public class Boats extends Module<Boats.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("newBoats")) {
            JsonObject newBoats = config.getAsJsonObject("newBoats");

            JsonOps.ifPresent(newBoats, "isFurnaceBoatOn", e -> this.config().isFurnaceBoatOn = e.getAsBoolean());
            JsonOps.ifPresent(newBoats, "isTNTBoatOn", e -> this.config().isTNTBoatOn = e.getAsBoolean());
            JsonOps.ifPresent(newBoats, "isJukeboxBoatOn", e -> this.config().isJukeboxBoatOn = e.getAsBoolean());
            JsonOps.ifPresent(newBoats, "isHopperBoatOn", e -> this.config().isHopperBoatOn = e.getAsBoolean());
        }
    }

    @Override
    public void onMain() {
        Common.bootstrap(this, BoatItems.class, BoatEntities.class);
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isFurnaceBoatOn = false;

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isTNTBoatOn = false;

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isJukeboxBoatOn = false;

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isHopperBoatOn = false;
    }
}
