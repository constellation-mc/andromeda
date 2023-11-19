package me.melontini.andromeda.modules.mechanics.throwable_items;

import com.google.common.collect.Lists;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

public class ThrowableItems implements Module<ThrowableItems.Config> {

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
    }
}
