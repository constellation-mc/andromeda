package me.melontini.andromeda.modules.items.balanced_mending;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;

public class BalancedMending implements Module {

    @Override
    public boolean enabled() {
        return Config.get().balancedMending;
    }
}
