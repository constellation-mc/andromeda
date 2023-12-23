package me.melontini.andromeda.modules.items.lockpick;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.JsonOps;
import me.melontini.dark_matter.api.base.util.MathStuff;

@Unscoped
@ModuleInfo(name = "lockpick", category = "items")
public class Lockpick extends Module<Lockpick.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        JsonOps.ifPresent(config, "lockpickEnabled", e -> this.config().enabled = e.getAsBoolean());

        if (config.has("lockpick")) {
            JsonObject lockpick = config.getAsJsonObject("lockpick");

            JsonOps.ifPresent(lockpick, "chance", e -> this.config().chance = e.getAsInt());
            JsonOps.ifPresent(lockpick, "breakAfterUse", e -> this.config().breakAfterUse = e.getAsBoolean());
            JsonOps.ifPresent(lockpick, "villagerInventory", e -> this.config().villagerInventory = e.getAsBoolean());
        }
    }

    public boolean rollLockpick() {
        return this.config().chance - 1 == 0 || MathStuff.threadRandom().nextInt(this.config().chance - 1) == 0;
    }

    public static class Config extends BasicConfig {

        public int chance = 3;

        public boolean breakAfterUse = true;

        public boolean villagerInventory = true;
    }
}
