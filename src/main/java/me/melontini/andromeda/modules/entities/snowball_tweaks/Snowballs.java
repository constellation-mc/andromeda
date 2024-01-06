package me.melontini.andromeda.modules.entities.snowball_tweaks;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.util.JsonOps;

@ModuleInfo(name = "snowball_tweaks", category = "entities", environment = Environment.SERVER)
public class Snowballs extends Module<Snowballs.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("snowballs")) {
            JsonObject snowballs = config.getAsJsonObject("snowballs");

            JsonOps.ifPresent(snowballs, "freeze", e -> this.config().freeze = e.getAsBoolean());
            JsonOps.ifPresent(snowballs, "extinguish", e -> this.config().extinguish = e.getAsBoolean());
            JsonOps.ifPresent(snowballs, "melt", e -> this.config().melt = e.getAsBoolean());
            JsonOps.ifPresent(snowballs, "layers", e -> this.config().layers = e.getAsBoolean());
            JsonOps.ifPresent(snowballs, "enableCooldown", e -> this.config().enableCooldown = e.getAsBoolean());
            JsonOps.ifPresent(snowballs, "cooldown", e -> this.config().cooldown = e.getAsInt());
        }
    }

    public static class Config extends BaseConfig {

        public boolean freeze = true;

        public boolean extinguish = true;

        public boolean melt = true;

        public boolean layers = false;

        public boolean enableCooldown = true;

        public int cooldown = 10;
    }
}
