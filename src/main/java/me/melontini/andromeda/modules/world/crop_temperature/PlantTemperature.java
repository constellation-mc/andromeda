package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.registries.Common;

@ModuleTooltip
@FeatureEnvironment(Environment.SERVER)
public class PlantTemperature extends BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(PlantTemperatureData.class);
    }
}
