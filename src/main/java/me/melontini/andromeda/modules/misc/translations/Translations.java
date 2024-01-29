package me.melontini.andromeda.modules.misc.translations;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;

@OldConfigKey("autoUpdateTranslations")
@ModuleInfo(name = "translations", category = "misc", environment = Environment.CLIENT)
public class Translations extends Module<Module.BaseConfig> {

}
