package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.base.events.LegacyConfigEvent;
import me.melontini.andromeda.util.JsonOps;

@Unscoped
@ModuleInfo(name = "minecart_block_picking", category = "items")
public class MinecartBlockPicking extends Module<MinecartBlockPicking.Config> {

    MinecartBlockPicking() {
        LegacyConfigEvent.BUS.listen(config -> {
            JsonOps.ifPresent(config, "minecartBlockPicking", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(config, "minecartSpawnerPicking", e -> this.config().spawnerPicking = e.getAsBoolean());
        });
    }

    public static class Config extends BaseConfig {

        public boolean spawnerPicking = false;
    }
}
