package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class GoatHorn implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}
