package me.melontini.andromeda.modules.blocks.bed.power;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;

@ModuleInfo(name = "bed/power", category = "blocks", environment = Environment.SERVER)
public class Power extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    Power() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends GameConfig {
        public NumberIntermediary power = NumberIntermediary.of(5);
    }
}
