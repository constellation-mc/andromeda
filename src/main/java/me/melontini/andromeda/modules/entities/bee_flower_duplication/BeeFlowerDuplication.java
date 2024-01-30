package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.events.LegacyConfigEvent;
import me.melontini.andromeda.util.JsonOps;

@ModuleInfo(name = "bee_flower_duplication", category = "entities", environment = Environment.SERVER)
public class BeeFlowerDuplication extends Module<BeeFlowerDuplication.Config> {

    BeeFlowerDuplication() {
        LegacyConfigEvent.BUS.listen(config -> {
            JsonOps.ifPresent(config, "beeFlowerDuplication", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(config, "beeTallFlowerDuplication", e -> this.config().tallFlowers = e.getAsBoolean());
        });
    }

    public static class Config extends BaseConfig {
        public boolean tallFlowers = true;
    }
}
