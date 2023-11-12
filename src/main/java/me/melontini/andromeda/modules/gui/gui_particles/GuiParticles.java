package me.melontini.andromeda.modules.gui.gui_particles;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class GuiParticles implements Module {

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
        return true;
    }
}
