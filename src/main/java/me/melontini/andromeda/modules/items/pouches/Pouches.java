package me.melontini.andromeda.modules.items.pouches;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.items.pouches.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;


@ModuleInfo(name = "pouches", category = "items")
public final class Pouches extends Module {

    public static final ConfigDefinition<Config> MAIN_CONFIG = new ConfigDefinition<>(() -> Config.class);

    Pouches() {
        this.defineConfig(ConfigState.MAIN, MAIN_CONFIG);
        InitEvent.main(this).listen((() -> () -> Main.init(this, Andromeda.ROOT_HANDLER.get(MAIN_CONFIG))));
        InitEvent.client(this).listen(() -> Client::init);

        InitEvent.client(this).listen(() -> () -> Main.testBlocks(this));
        InitEvent.server(this).listen(() -> () -> Main.testBlocks(this));
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
