package me.melontini.andromeda.modules.world.quick_fire;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("quickFire")
@ModuleInfo(name = "quick_fire", category = "world", environment = Environment.SERVER)
public class QuickFire extends Module<BasicConfig> {

}
