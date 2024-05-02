package me.melontini.andromeda.modules.entities.snowball_tweaks;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;

@ModuleInfo(name = "snowball_tweaks", category = "entities", environment = Environment.SERVER)
public class Snowballs extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    Snowballs() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends GameConfig {

        public boolean freeze = true;

        public boolean extinguish = true;

        public boolean melt = true;

        public boolean layers = false;

        public boolean enableCooldown = true;

        public NumberIntermediary cooldown = NumberIntermediary.of(10);
    }
}
