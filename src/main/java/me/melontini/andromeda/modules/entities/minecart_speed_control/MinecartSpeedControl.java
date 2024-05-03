package me.melontini.andromeda.modules.entities.minecart_speed_control;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;

@ModuleInfo(name = "minecart_speed_control", category = "entities", environment = Environment.SERVER)
public class MinecartSpeedControl extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    MinecartSpeedControl() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends Module.GameConfig {
        public NumberIntermediary modifier = NumberIntermediary.of(1d);
        public NumberIntermediary furnaceModifier = NumberIntermediary.of(1d);
        public NumberIntermediary additionalFurnaceFuel = NumberIntermediary.of(0);
    }
}
