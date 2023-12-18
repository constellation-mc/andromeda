package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.annotations.GameRule;
import me.melontini.andromeda.common.registries.Common;

@OldConfigKey("temperatureBasedCropGrowthSpeed")
@ModuleTooltip
@ModuleInfo(name = "crop_temperature", category = "world", environment = Environment.SERVER)
public class PlantTemperature extends Module<PlantTemperature.Config> {

    @Override
    public void onMain() {
        Common.bootstrap(this, PlantTemperatureData.class, Content.class);
    }

    public static class Config extends BasicConfig {
        @GameRule
        public boolean affectBoneMeal = true;
    }
}
