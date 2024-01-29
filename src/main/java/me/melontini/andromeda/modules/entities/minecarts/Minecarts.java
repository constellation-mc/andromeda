package me.melontini.andromeda.modules.entities.minecarts;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Unscoped
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
