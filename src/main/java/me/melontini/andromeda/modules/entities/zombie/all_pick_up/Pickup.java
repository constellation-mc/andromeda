package me.melontini.andromeda.modules.entities.zombie.all_pick_up;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("allZombiesCanPickUpItems")
@ModuleInfo(name = "zombie/all_pick_up", category = "entities", environment = Environment.SERVER)
public class Pickup extends Module<BasicConfig> {

}
