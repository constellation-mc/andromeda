package me.melontini.andromeda.modules.items.pouches;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.items.pouches.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Unscoped
@ModuleInfo(name = "pouches", category = "items")
public final class Pouches extends Module {

    public static final ConfigDefinition<Config> MAIN_CONFIG = new ConfigDefinition<>(() -> Config.class);

    Pouches() {
        this.defineConfig(ConfigState.MAIN, MAIN_CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));

        InitEvent.client(this).listen(() -> List.of(Merged.class));
        InitEvent.server(this).listen(() -> List.of(Merged.class));
    }

    @ToString
    public static class Config extends BaseConfig {
        @ConfigEntry.Gui.RequiresRestart
        public boolean seedPouch = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean flowerPouch = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean saplingPouch = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean specialPouch = false;
    }
}
