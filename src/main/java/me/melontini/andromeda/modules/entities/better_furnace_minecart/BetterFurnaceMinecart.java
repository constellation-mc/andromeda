package me.melontini.andromeda.modules.entities.better_furnace_minecart;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@ModuleInfo(name = "better_furnace_minecart", category = "entities", environment = Environment.SERVER)
public final class BetterFurnaceMinecart extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    BetterFurnaceMinecart() {
        this.defineConfig(ConfigState.MAIN, CONFIG);
    }

    @ToString
    public static final class Config extends BaseConfig {
        public int maxFuel = 45000;
        public boolean takeFuelWhenLow = true;
    }
}
