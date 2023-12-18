package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.OldConfigKey;

@OldConfigKey("minorInconvenience")
@ModuleTooltip
@ModuleInfo(name = "minor_inconvenience", category = "misc")
public class MinorInconvenience extends BasicModule {

    @Override
    public void onMain() {
        Agony.init();
    }
}
