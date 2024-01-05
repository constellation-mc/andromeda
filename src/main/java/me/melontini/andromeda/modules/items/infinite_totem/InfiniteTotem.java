package me.melontini.andromeda.modules.items.infinite_totem;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.util.JsonOps;

@ModuleInfo(name = "infinite_totem", category = "items")
public class InfiniteTotem extends Module<InfiniteTotem.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("totemSettings")) {
            JsonObject totemSettings = config.getAsJsonObject("totemSettings");

            JsonOps.ifPresent(totemSettings, "enableInfiniteTotem", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(totemSettings, "enableTotemAscension", e -> this.config().enableAscension = e.getAsBoolean());
        }
    }

    public static class Config extends BaseConfig {

        public boolean enableAscension = true;
    }
}
