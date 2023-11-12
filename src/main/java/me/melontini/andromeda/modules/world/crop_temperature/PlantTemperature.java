package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class PlantTemperature implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(PlantTemperatureData.class);
    }

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return Config.get().temperatureBasedCropGrowthSpeed;
    }
}
