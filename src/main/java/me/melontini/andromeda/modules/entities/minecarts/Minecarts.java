package me.melontini.andromeda.modules.entities.minecarts;

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
@ModuleInfo(name = "minecarts", category = "entities")
public class Minecarts extends Module<Minecarts.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("newMinecarts")) {
            JsonObject newMinecarts = config.getAsJsonObject("newMinecarts");

            JsonOps.ifPresent(newMinecarts, "isAnvilMinecartOn", e -> this.config().isAnvilMinecartOn = e.getAsBoolean());
            JsonOps.ifPresent(newMinecarts, "isNoteBlockMinecartOn", e -> this.config().isNoteBlockMinecartOn = e.getAsBoolean());
            JsonOps.ifPresent(newMinecarts, "isJukeboxMinecartOn", e -> this.config().isJukeboxMinecartOn = e.getAsBoolean());
        }
    }

    @Override
    public void onMain() {
        Common.bootstrap(this, MinecartItems.class, MinecartEntities.class);
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isAnvilMinecartOn = false;

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isNoteBlockMinecartOn = false;

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isJukeboxMinecartOn = false;

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.BOTH)
        public boolean isSpawnerMinecartOn = false;
    }
}
