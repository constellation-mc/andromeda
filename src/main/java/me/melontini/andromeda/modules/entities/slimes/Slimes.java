package me.melontini.andromeda.modules.entities.slimes;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.events.LegacyConfigEvent;
import me.melontini.andromeda.util.JsonOps;

@ModuleInfo(name = "slimes", category = "entities", environment = Environment.SERVER)
public class Slimes extends Module<Slimes.Config> {

    Slimes() {
        LegacyConfigEvent.BUS.listen(config -> {
            if (config.has("slimes")) {
                JsonObject slimes = config.getAsJsonObject("slimes");

                JsonOps.ifPresent(slimes, "flee", e -> this.config().flee = e.getAsBoolean());
                JsonOps.ifPresent(slimes, "merge", e -> this.config().merge = e.getAsBoolean());
                JsonOps.ifPresent(slimes, "maxMerge", e -> this.config().maxMerge = e.getAsInt());
                JsonOps.ifPresent(slimes, "slowness", e -> this.config().slowness = e.getAsBoolean());
            }
        });
    }

    public static class Config extends BaseConfig {

        public boolean flee = true;

        public boolean merge = true;

        public int maxMerge = 4;

        public boolean slowness = false;
    }
}
