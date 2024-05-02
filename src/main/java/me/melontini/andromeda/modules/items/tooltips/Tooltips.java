package me.melontini.andromeda.modules.items.tooltips;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "tooltips", category = "items", environment = Environment.CLIENT)
public class Tooltips extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    Tooltips() {
        this.defineConfig(ConfigState.MAIN, CONFIG);
    }

    @ToString
    public static class Config extends BaseConfig {
        public boolean clock = true;
        public boolean compass = true;
        public boolean recoveryCompass = true;
    }
}
