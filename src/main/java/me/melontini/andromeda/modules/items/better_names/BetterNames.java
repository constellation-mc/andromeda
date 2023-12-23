package me.melontini.andromeda.modules.items.better_names;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("slightlyBetterItemNames")
@ModuleInfo(name = "better_names", category = "items", environment = Environment.CLIENT)
public class BetterNames extends Module<BasicConfig> {

}
