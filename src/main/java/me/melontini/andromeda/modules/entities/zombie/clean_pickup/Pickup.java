package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.events.LegacyConfigEvent;

@ModuleInfo(name = "zombie/clean_pickup", category = "entities", environment = Environment.SERVER)
public class Pickup extends Module<Module.BaseConfig> {

    Pickup() {
        LegacyConfigEvent.BUS.listen(config -> {
            if (config.has("newThrowableItems")) {
                JsonObject o = config.get("newThrowableItems").getAsJsonObject();
                this.config().enabled = o.has("enable") && o.get("enable").getAsBoolean()
                        && o.has("preventUselessItems") && o.get("preventUselessItems").getAsBoolean();
            }
        });
    }
}
