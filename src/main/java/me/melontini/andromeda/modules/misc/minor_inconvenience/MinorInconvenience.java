package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.Module;

public class MinorInconvenience implements Module {

    @Override
    public void onMain() {
        Agony.init();
    }
}
