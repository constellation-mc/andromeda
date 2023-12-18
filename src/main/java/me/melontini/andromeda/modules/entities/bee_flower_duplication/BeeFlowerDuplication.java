package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip(2)
@ModuleInfo(name = "bee_flower_duplication", category = "entities", environment = Environment.SERVER)
public class BeeFlowerDuplication extends Module<BeeFlowerDuplication.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        JsonOps.ifPresent(config, "beeFlowerDuplication", e -> this.config().enabled = e.getAsBoolean());
        JsonOps.ifPresent(config, "beeTallFlowerDuplication", e -> this.config().tallFlowers = e.getAsBoolean());
    }

    public static class Config extends BasicConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean tallFlowers = true;
    }
}
