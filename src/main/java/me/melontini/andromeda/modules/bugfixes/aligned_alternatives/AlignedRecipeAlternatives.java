package me.melontini.andromeda.modules.bugfixes.aligned_alternatives;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;

@OldConfigKey("properlyAlignedRecipeAlternatives")
@SpecialEnvironment(Environment.CLIENT)
@ModuleInfo(name = "aligned_alternatives", category = "bugfixes", environment = Environment.CLIENT)
public class AlignedRecipeAlternatives extends BasicModule {

}
