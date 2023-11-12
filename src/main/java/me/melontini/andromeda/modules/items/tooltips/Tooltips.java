package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Tooltips implements Module {

    @Override
    public Environment environment() {
        return Environment.CLIENT;
    }

    @Override
    public boolean enabled() {
        return true;//TODO
    }
}
