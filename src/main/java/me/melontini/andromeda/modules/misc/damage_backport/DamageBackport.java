package me.melontini.andromeda.modules.misc.damage_backport;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.registries.Common;

@FeatureEnvironment(Environment.SERVER)
public class DamageBackport implements BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(DamageCommand.class);
    }
}
