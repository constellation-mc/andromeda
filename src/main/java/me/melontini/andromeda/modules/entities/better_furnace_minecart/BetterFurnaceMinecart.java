package me.melontini.andromeda.modules.entities.better_furnace_minecart;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;

@FeatureEnvironment(Environment.SERVER)
public class BetterFurnaceMinecart implements Module<BetterFurnaceMinecart.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {
        public int maxFuel = 45000;
        public boolean takeFuelWhenLow = true;
    }
}
