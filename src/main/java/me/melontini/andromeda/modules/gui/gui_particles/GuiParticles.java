package me.melontini.andromeda.modules.gui.gui_particles;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.base.Environment;

@FeatureEnvironment(Environment.CLIENT)
public class GuiParticles implements Module {

    @Override
    public Class<? extends BasicConfig> configClass() {
        return Config.class;
    }
}
