package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("unknown")
@ModuleInfo(name = "unknown", category = "misc")
public class Unknown extends Module<BasicConfig> {

    public static String DEBUG_SPLASH;
}
