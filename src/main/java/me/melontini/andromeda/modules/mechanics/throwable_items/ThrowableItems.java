package me.melontini.andromeda.modules.mechanics.throwable_items;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.modules.mechanics.throwable_items.client.Client;
import me.melontini.andromeda.util.commander.CommanderSupport;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;

import java.util.List;

@ModuleInfo(name = "throwable_items", category = "mechanics")
public final class ThrowableItems extends Module {

    public static final ConfigDefinition<ClientConfig> CLIENT_CONFIG = new ConfigDefinition<>(() ->  ClientConfig.class);
    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    ThrowableItems() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        this.defineConfig(ConfigState.CLIENT, CLIENT_CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));

        CommanderSupport.require(this);
    }

    public static class ClientConfig extends BaseConfig {
        public boolean tooltip = true;
    }

    @ToString
    public static class Config extends BaseConfig {
        public boolean canZombiesThrowItems = true;
        public DoubleIntermediary zombieThrowInterval = DoubleIntermediary.of(40);
    }
}
