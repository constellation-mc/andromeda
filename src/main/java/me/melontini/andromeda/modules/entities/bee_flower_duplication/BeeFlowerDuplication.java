package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "bee_flower_duplication", category = "entities", environment = Environment.SERVER)
public class BeeFlowerDuplication extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    BeeFlowerDuplication() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends GameConfig {
        public boolean tallFlowers = true;
    }
}
