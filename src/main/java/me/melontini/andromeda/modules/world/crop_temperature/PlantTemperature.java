package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "crop_temperature", category = "world", environment = Environment.SERVER)
public class PlantTemperature extends Module<PlantTemperature.Config> {

    PlantTemperature() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    public static class Config extends BaseConfig {
        public boolean affectBoneMeal = true;
    }
}
