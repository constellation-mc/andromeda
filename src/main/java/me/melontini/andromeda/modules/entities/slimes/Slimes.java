package me.melontini.andromeda.modules.entities.slimes;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "slimes", category = "entities", environment = Environment.SERVER)
public class Slimes extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    Slimes() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends GameConfig {

        public boolean flee = true;

        public boolean merge = true;

        public int maxMerge = 4;

        public boolean slowness = false;
    }
}
