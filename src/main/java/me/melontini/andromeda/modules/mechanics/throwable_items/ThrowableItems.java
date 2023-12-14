package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip
@ModuleInfo(name = "throwable_items", category = "mechanics")
public class ThrowableItems extends Module<ThrowableItems.Config> {

    @Override
    public void onMain() {
        Common.bootstrap(Content.class, ItemBehaviorData.class);
    }

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.SERVER)
        public boolean canZombiesThrowItems = true;

        @ConfigEntry.Gui.Tooltip
        @SpecialEnvironment(Environment.SERVER)
        public int zombieThrowInterval = 40;

        @SpecialEnvironment(Environment.BOTH)
        public boolean tooltip = true;
    }
}
