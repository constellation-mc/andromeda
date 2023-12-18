package me.melontini.andromeda.modules.blocks.guarded_loot;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip
@ModuleInfo(name = "guarded_loot", category = "blocks")
public class GuardedLoot extends Module<GuardedLoot.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("guardedLoot")) {
            JsonObject guardedLoot = config.getAsJsonObject("guardedLoot");

            JsonOps.ifPresent(guardedLoot, "enabled", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(guardedLoot, "range", e -> this.config().range = e.getAsInt());
        }
    }

    public static class Config extends BasicConfig {

        public int range = 4;

        @ConfigEntry.Gui.Tooltip
        public boolean allowLockPicking = true;
    }
}
