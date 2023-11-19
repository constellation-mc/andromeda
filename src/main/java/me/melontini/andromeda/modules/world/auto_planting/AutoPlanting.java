package me.melontini.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@FeatureEnvironment(Environment.SERVER)
public class AutoPlanting implements Module<AutoPlanting.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    @Override
    public Config config() {
        return Module.super.config();
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean blacklistMode = true;

        @ConfigEntry.Gui.Tooltip
        public List<String> idList = Lists.newArrayList();
    }
}
