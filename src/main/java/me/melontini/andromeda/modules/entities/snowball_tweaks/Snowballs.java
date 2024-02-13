package me.melontini.andromeda.modules.entities.snowball_tweaks;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;

@ModuleInfo(name = "snowball_tweaks", category = "entities", environment = Environment.SERVER)
public class Snowballs extends Module<Snowballs.Config> {

    Snowballs() {
    }

    public static class Config extends BaseConfig {

        public boolean freeze = true;

        public boolean extinguish = true;

        public boolean melt = true;

        public boolean layers = false;

        public boolean enableCooldown = true;

        public int cooldown = 10;
    }
}
