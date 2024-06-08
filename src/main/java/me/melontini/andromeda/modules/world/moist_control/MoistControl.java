package me.melontini.andromeda.modules.world.moist_control;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.number.LongIntermediary;

@ModuleInfo(name = "moist_control", category = "world", environment = Environment.SERVER)
public final class MoistControl extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    MoistControl() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends BaseConfig {
        public LongIntermediary customMoisture = LongIntermediary.of(4);
    }
}