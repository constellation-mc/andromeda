package me.melontini.andromeda.modules.items.tooltips;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "tooltips", category = "items", environment = Environment.CLIENT)
public class Tooltips extends Module<Tooltips.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("tooltips")) {
            JsonObject tooltips = config.getAsJsonObject("tooltips");

            this.config().enabled = true;
            JsonOps.ifPresent(tooltips, "clock", e -> this.config().clock = e.getAsBoolean());
            JsonOps.ifPresent(tooltips, "compass", e -> this.config().compass = e.getAsBoolean());
            JsonOps.ifPresent(tooltips, "recoveryCompass", e -> this.config().recoveryCompass = e.getAsBoolean());
        }
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean clock = true;

        @ConfigEntry.Gui.Tooltip
        public boolean compass = true;

        @ConfigEntry.Gui.Tooltip
        public boolean recoveryCompass = true;
    }
}
