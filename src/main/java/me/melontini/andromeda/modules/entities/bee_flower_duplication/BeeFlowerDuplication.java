package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;

@FeatureEnvironment(Environment.SERVER)
public class BeeFlowerDuplication implements Module<BeeFlowerDuplication.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {
        public boolean tallFlowers = true;
    }
}
