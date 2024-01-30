package me.melontini.andromeda.modules.blocks.incubator;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.base.events.LegacyConfigEvent;
import me.melontini.andromeda.util.JsonOps;

@Unscoped
@ModuleInfo(name = "incubator", category = "blocks")
public class Incubator extends Module<Incubator.Config> {

    Incubator() {
        LegacyConfigEvent.BUS.listen(config -> {
            if (config.has("incubatorSettings")) {
                JsonObject incubator = config.getAsJsonObject("incubatorSettings");

                JsonOps.ifPresent(incubator, "enableIncubator", e -> this.config().enabled = e.getAsBoolean());
                JsonOps.ifPresent(incubator, "incubatorRandomness", e -> this.config().randomness = e.getAsBoolean());
            }
        });
    }

    public static class Config extends BaseConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean randomness = true;
    }
}
