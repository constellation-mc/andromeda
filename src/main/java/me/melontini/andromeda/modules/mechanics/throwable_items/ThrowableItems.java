package me.melontini.andromeda.modules.mechanics.throwable_items;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.modules.mechanics.throwable_items.client.Client;
import me.melontini.andromeda.util.commander.CommanderSupport;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;


@ModuleInfo(name = "throwable_items", category = "mechanics")
public final class ThrowableItems extends Module {

    public static final ConfigDefinition<ClientConfig> CLIENT_CONFIG = new ConfigDefinition<>(() ->  ClientConfig.class);
    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    ThrowableItems() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        this.defineConfig(ConfigState.CLIENT, CLIENT_CONFIG);
        InitEvent.main(this).listen(() -> Main::init);
        InitEvent.client(this).listen(() -> () -> Client.init(AndromedaClient.HANDLER.get(CLIENT_CONFIG)));

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
