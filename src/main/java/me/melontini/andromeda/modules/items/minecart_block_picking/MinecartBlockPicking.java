package me.melontini.andromeda.modules.items.minecart_block_picking;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.JsonOps;

@Unscoped
@ModuleInfo(name = "minecart_block_picking", category = "items")
public class MinecartBlockPicking extends Module<MinecartBlockPicking.Config> {

    @Override
    public void onMain() {
        PlaceBehaviorHandler.init();
        PickUpBehaviorHandler.init();
    }

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        JsonOps.ifPresent(config, "minecartBlockPicking", e -> this.config().enabled = e.getAsBoolean());
        JsonOps.ifPresent(config, "minecartSpawnerPicking", e -> this.config().spawnerPicking = e.getAsBoolean());
    }

    public static class Config extends BasicConfig {

        public boolean spawnerPicking = false;
    }
}
