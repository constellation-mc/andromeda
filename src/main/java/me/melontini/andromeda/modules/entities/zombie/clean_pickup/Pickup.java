package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Pickup implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(PickupTag.class);
    }

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}
