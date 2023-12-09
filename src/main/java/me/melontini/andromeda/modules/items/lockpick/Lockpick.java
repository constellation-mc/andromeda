package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip
@ModuleInfo(name = "lockpick", category = "items")
public class Lockpick extends Module<Lockpick.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public boolean rollLockpick() {
        return this.config().chance - 1 == 0 || MathStuff.threadRandom().nextInt(this.config().chance - 1) == 0;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public int chance = 3;

        @ConfigEntry.Gui.Tooltip
        public boolean breakAfterUse = true;

        @ConfigEntry.Gui.Tooltip
        public boolean villagerInventory = true;
    }
}
