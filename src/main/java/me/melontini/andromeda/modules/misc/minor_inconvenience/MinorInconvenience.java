package me.melontini.andromeda.modules.misc.minor_inconvenience;

import me.melontini.andromeda.base.BasicModule;

public class MinorInconvenience implements BasicModule {

    @Override
    public void onMain() {
        Agony.init();
    }
}
