package me.melontini.andromeda.modules.blocks.bed.safe;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;

public class Safe implements Module {
    @Override
    public boolean enabled() {
        return Config.get().safeBeds;
    }
}
