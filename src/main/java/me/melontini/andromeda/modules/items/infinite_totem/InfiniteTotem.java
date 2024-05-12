package me.melontini.andromeda.modules.items.infinite_totem;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.modules.items.infinite_totem.client.Client;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;

import java.util.List;

@ModuleInfo(name = "infinite_totem", category = "items")
public final class InfiniteTotem extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    InfiniteTotem() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    @ToString
    public static final class Config extends GameConfig {
        public BooleanIntermediary enableAscension = BooleanIntermediary.of(true);
    }
}
