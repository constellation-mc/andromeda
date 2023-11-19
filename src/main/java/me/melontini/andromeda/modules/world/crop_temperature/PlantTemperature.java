package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.base.Environment;

@FeatureEnvironment(Environment.SERVER)
public class PlantTemperature implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(PlantTemperatureData.class);
    }
}
