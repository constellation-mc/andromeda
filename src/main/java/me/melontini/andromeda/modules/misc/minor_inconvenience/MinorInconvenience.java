package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.annotations.ModuleTooltip;

@ModuleTooltip
public class MinorInconvenience implements BasicModule {

    @Override
    public void onMain() {
        Agony.init();
    }
}
