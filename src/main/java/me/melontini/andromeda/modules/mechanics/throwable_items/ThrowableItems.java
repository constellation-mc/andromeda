package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;

@Unscoped
@ModuleInfo(name = "throwable_items", category = "mechanics")
public class ThrowableItems extends Module<ThrowableItems.Config> {

    ThrowableItems() {
    }

    public static class Config extends BaseConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean canZombiesThrowItems = true;

        @SpecialEnvironment(Environment.SERVER)
        public int zombieThrowInterval = 40;

        @SpecialEnvironment(Environment.BOTH)
        public boolean tooltip = true;
    }
}
