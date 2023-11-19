package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.registries.Common;

@FeatureEnvironment(Environment.SERVER)
public class Pickup implements BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(PickupTag.class);
    }
}
