package me.melontini.andromeda.modules.blocks.incubator;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.andromeda.util.JsonOps;

@ModuleInfo(name = "incubator", category = "blocks")
public class Incubator extends Module<Incubator.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("incubatorSettings")) {
            JsonObject incubator = config.getAsJsonObject("incubatorSettings");

            JsonOps.ifPresent(incubator, "enableIncubator", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(incubator, "incubatorRandomness", e -> this.config().randomness = e.getAsBoolean());
        }
    }

    @Override
    public void onMain() {
        Common.bootstrap(this, Content.class, EggProcessingData.class);
    }

    public static class Config extends BasicConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean randomness = true;
    }
}
