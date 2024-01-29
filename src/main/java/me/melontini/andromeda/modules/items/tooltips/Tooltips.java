package me.melontini.andromeda.modules.items.tooltips;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.util.JsonOps;

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

    public static class Config extends BaseConfig {

        public boolean clock = true;
        public boolean compass = true;
        public boolean recoveryCompass = true;
    }
}
