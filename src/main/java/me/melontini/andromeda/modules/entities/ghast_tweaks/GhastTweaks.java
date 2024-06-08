package me.melontini.andromeda.modules.entities.ghast_tweaks;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;


@ModuleInfo(name = "ghast_tweaks", category = "entities", environment = Environment.SERVER)
public final class GhastTweaks extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    GhastTweaks() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> Main::init);
    }

    @ToString
    public static class Config extends Module.GameConfig {
        public BooleanIntermediary explodeOnDeath = BooleanIntermediary.of(false);
        public DoubleIntermediary explosionPower = DoubleIntermediary.of(4);
    }
}
