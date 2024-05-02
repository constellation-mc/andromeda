package me.melontini.andromeda.modules.items.minecart_block_picking;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;

import java.util.List;

@Unscoped
@ModuleInfo(name = "minecart_block_picking", category = "items", environment = Environment.SERVER)
public class MinecartBlockPicking extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    MinecartBlockPicking() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    @ToString
    public static class Config extends BaseConfig {
        public boolean spawnerPicking = false;
    }
}
