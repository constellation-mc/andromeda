package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;

@OldConfigKey("temperatureBasedCropGrowthSpeed")
@ModuleInfo(name = "crop_temperature", category = "world", environment = Environment.SERVER)
public class PlantTemperature extends Module<PlantTemperature.Config> {

    public static class Config extends BaseConfig {
        public boolean affectBoneMeal = true;
    }
}
