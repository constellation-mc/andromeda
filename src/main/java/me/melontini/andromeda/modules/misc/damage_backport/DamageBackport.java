package me.melontini.andromeda.modules.misc.damage_backport;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.base.Environment;

@FeatureEnvironment(Environment.SERVER)
public class DamageBackport implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(DamageCommand.class);
    }
}
