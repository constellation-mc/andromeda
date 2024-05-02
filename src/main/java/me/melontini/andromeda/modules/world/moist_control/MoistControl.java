package me.melontini.andromeda.modules.world.moist_control;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "moist_control", category = "world", environment = Environment.SERVER)
public class MoistControl extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    MoistControl() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends BaseConfig {
        public int customMoisture = 4;
    }
}