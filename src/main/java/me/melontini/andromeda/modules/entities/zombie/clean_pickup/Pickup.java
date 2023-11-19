package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.base.Environment;

@FeatureEnvironment(Environment.SERVER)
public class Pickup implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(PickupTag.class);
    }
}
