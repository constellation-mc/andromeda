package me.melontini.andromeda.modules.world.falling_beenests;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("canBeeNestsFall")
@ModuleInfo(name = "falling_beenests", category = "world", environment = Environment.SERVER)
public class CanBeeNestsFall extends Module<BasicConfig> {

}
