package me.melontini.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Origin;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.JsonOps;

import java.util.ArrayList;
import java.util.List;

@Origin(mod = "TinyTweaks", author = "HephaestusDev")
@ModuleInfo(name = "auto_planting", category = "world", environment = Environment.SERVER)
public class AutoPlanting extends Module<AutoPlanting.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("autoPlanting")) {
            JsonObject ap = config.getAsJsonObject("autoPlanting");

            JsonOps.ifPresent(ap, "enabled", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(ap, "blacklistMode", e -> this.config().blacklistMode = e.getAsBoolean());

            JsonOps.ifPresent(ap, "idList", element -> {
                List<String> ids = new ArrayList<>();
                for (JsonElement e : element.getAsJsonArray()) {
                    ids.add(e.getAsString());
                }
                this.config().idList = ids;
            });
        }
    }

    public static class Config extends BasicConfig {

        public boolean blacklistMode = true;

        public List<String> idList = Lists.newArrayList();
    }
}
