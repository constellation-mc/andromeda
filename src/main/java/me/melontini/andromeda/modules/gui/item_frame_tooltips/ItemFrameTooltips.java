package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class ItemFrameTooltips implements Module {

    @Override
    public void onClient() {
        Common.bootstrap(Client.class);
    }

    @Override
    public Environment environment() {
        return Environment.CLIENT;
    }

    @Override
    public boolean enabled() {
        return Config.get().itemFrameTooltips;
    }
}
