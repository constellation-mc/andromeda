package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;

public class MinorInconvenience implements Module {

    @Override
    public void onMain() {
        Agony.init();
    }

    @Override
    public boolean enabled() {
        return Config.get().minorInconvenience;
    }
}
