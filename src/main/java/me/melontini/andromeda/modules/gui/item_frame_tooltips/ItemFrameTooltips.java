package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class ItemFrameTooltips implements Module {

    @Override
    public Environment environment() {
        return Environment.CLIENT;
    }
}
