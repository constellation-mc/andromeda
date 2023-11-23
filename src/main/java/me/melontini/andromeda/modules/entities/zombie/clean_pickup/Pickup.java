package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.registries.Common;

@ModuleTooltip(3)
@FeatureEnvironment(Environment.SERVER)
public class Pickup extends BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(PickupTag.class);
    }
}
