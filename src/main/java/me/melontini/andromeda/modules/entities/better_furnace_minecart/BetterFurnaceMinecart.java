package me.melontini.andromeda.modules.entities.better_furnace_minecart;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Unscoped;

@Unscoped
@ModuleInfo(name = "better_furnace_minecart", category = "entities", environment = Environment.SERVER)
public class BetterFurnaceMinecart extends Module<BetterFurnaceMinecart.Config> {

    BetterFurnaceMinecart() {
    }

    public static class Config extends BaseConfig {
        public int maxFuel = 45000;

        public boolean takeFuelWhenLow = true;
    }
}
