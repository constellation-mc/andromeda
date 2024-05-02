package me.melontini.andromeda.modules.entities.boats;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.entities.boats.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Unscoped
@ModuleInfo(name = "boats", category = "entities")
public class Boats extends Module {

    public static final ConfigDefinition<Config> MAIN_CONFIG = new ConfigDefinition<>(() -> Config.class);

    Boats() {
        this.defineConfig(ConfigState.MAIN, MAIN_CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    @ToString
    public static class Config extends BaseConfig {

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isFurnaceBoatOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isTNTBoatOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isJukeboxBoatOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isHopperBoatOn = false;
    }
}
