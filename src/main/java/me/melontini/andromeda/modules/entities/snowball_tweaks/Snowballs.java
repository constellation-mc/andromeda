package me.melontini.andromeda.modules.entities.snowball_tweaks;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;

@ModuleInfo(name = "snowball_tweaks", category = "entities", environment = Environment.SERVER)
public class Snowballs extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    Snowballs() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends GameConfig {

        public BooleanIntermediary freeze = BooleanIntermediary.of(true);

        public BooleanIntermediary extinguish = BooleanIntermediary.of(true);

        public BooleanIntermediary melt = BooleanIntermediary.of(true);

        public BooleanIntermediary layers = BooleanIntermediary.of(false);

        public BooleanIntermediary enableCooldown = BooleanIntermediary.of(true);

        public NumberIntermediary cooldown = NumberIntermediary.of(10);
    }
}
