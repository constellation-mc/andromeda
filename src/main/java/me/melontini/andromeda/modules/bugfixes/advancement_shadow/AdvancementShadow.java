package me.melontini.andromeda.modules.bugfixes.advancement_shadow;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("frameIndependentAdvancementShadow")
@ModuleInfo(name = "advancement_shadow", category = "bugfixes", environment = Environment.CLIENT)
public class AdvancementShadow extends Module<BasicConfig> {

}
