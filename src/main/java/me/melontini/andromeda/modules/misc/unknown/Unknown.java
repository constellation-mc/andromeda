package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.registries.Common;

public class Unknown implements Module {

    public static String DEBUG_SPLASH;

    @Override
    public void onClient() {
        Common.bootstrap(UnknownClient.class);
    }

    @Override
    public void onMain() {
        Common.bootstrap(UnknownContent.class);
    }
}
