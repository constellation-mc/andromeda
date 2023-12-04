package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.common.registries.Common;

@ModuleTooltip(3)
@ModuleInfo(name = "zombie/clean_pickup", category = "entities", environment = Environment.SERVER)
public class Pickup extends BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(PickupTag.class);
    }
}
