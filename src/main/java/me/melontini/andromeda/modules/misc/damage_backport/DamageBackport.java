package me.melontini.andromeda.modules.misc.damage_backport;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class DamageBackport implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(DamageCommand.class);
    }

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return Config.get().damageBackport;
    }
}
