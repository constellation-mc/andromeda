package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.dark_matter.api.base.util.MathStuff;

@Unscoped
@ModuleInfo(name = "lockpick", category = "items")
public class Lockpick extends Module<Lockpick.Config> {

    Lockpick() {
    }

    public boolean rollLockpick() {
        return this.config().chance - 1 == 0 || MathStuff.threadRandom().nextInt(this.config().chance - 1) == 0;
    }

    public static class Config extends BaseConfig {

        public int chance = 3;

        public boolean breakAfterUse = true;

        public boolean villagerInventory = true;
    }
}
