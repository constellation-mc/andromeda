package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip(2)
@FeatureEnvironment(Environment.SERVER)
public class BeeFlowerDuplication extends Module<BeeFlowerDuplication.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean tallFlowers = true;
    }
}
