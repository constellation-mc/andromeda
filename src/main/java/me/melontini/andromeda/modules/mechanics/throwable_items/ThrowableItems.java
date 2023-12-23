package me.melontini.andromeda.modules.mechanics.throwable_items;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.util.JsonOps;

@Unscoped
@ModuleInfo(name = "throwable_items", category = "mechanics")
public class ThrowableItems extends Module<ThrowableItems.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("newThrowableItems")) {
            JsonObject newThrowableItems = config.getAsJsonObject("newThrowableItems");

            JsonOps.ifPresent(newThrowableItems, "enable", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(newThrowableItems, "canZombiesThrowItems", e -> this.config().canZombiesThrowItems = e.getAsBoolean());
            JsonOps.ifPresent(newThrowableItems, "zombieThrowInterval", e -> this.config().zombieThrowInterval = e.getAsInt());
            JsonOps.ifPresent(newThrowableItems, "tooltip", e -> this.config().tooltip = e.getAsBoolean());
        }
    }

    @Override
    public void onMain() {
        Common.bootstrap(this, Content.class, ItemBehaviorData.class);
    }

    public static class Config extends BasicConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean canZombiesThrowItems = true;

        @SpecialEnvironment(Environment.SERVER)
        public int zombieThrowInterval = 40;

        @SpecialEnvironment(Environment.BOTH)
        public boolean tooltip = true;
    }
}
