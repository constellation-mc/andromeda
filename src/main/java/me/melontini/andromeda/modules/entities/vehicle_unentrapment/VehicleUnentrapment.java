package me.melontini.andromeda.modules.entities.vehicle_unentrapment;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "vehicle_unentrapment", category = "entities", environment = Environment.SERVER)
public class VehicleUnentrapment extends Module<Module.BaseConfig> {

    VehicleUnentrapment() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
