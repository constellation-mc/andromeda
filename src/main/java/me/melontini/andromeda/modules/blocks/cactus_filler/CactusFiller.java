package me.melontini.andromeda.modules.blocks.cactus_filler;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;

public class CactusFiller implements Module {

    @Override
    public boolean enabled() {
        return Config.get().cactusBottleFilling;
    }
}
