package me.melontini.andromeda.modules.gui.name_tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class NameTooltips implements Module {
    @Override
    public Environment environment() {
        return Environment.CLIENT;
    }
}
