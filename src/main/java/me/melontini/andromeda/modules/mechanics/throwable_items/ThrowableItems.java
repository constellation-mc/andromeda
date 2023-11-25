package me.melontini.andromeda.modules.mechanics.throwable_items;

import com.google.common.collect.Lists;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.registries.Common;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

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
        public List<String> blacklist = Lists.newArrayList();

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.SERVER)
        public boolean canZombiesThrowItems = true;

        @ConfigEntry.Gui.Tooltip
        public int zombieThrowInterval = 40;

        @FeatureEnvironment(Environment.CLIENT)
        public boolean tooltip = true;
    }
}
