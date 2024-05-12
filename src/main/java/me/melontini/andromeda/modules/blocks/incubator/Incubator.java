package me.melontini.andromeda.modules.blocks.incubator;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.modules.blocks.incubator.client.Client;
import me.melontini.andromeda.util.commander.CommanderSupport;

import java.util.List;

@ModuleInfo(name = "incubator", category = "blocks")
public final class Incubator extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    Incubator() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));

        CommanderSupport.require(this);
    }

    @ToString
    public static class Config extends BaseConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean randomness = true;
    }
}
