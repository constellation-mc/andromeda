package me.melontini.andromeda.modules.bugfixes.aligned_alternatives;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("properlyAlignedRecipeAlternatives")
@SpecialEnvironment(Environment.CLIENT)
@ModuleInfo(name = "aligned_alternatives", category = "bugfixes", environment = Environment.CLIENT)
public class AlignedRecipeAlternatives extends Module<BasicConfig> {

}
