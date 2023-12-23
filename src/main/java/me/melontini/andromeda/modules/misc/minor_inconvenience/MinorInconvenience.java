package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;

@OldConfigKey("minorInconvenience")
@ModuleInfo(name = "minor_inconvenience", category = "misc")
public class MinorInconvenience extends Module<BasicConfig> {

    @Override
    public void onMain() {
        Agony.init();
    }
}
