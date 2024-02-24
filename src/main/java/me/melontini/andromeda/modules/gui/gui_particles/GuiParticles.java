package me.melontini.andromeda.modules.gui.gui_particles;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "gui_particles", category = "gui", environment = Environment.CLIENT)
public class GuiParticles extends Module<GuiParticles.Config> {

    GuiParticles() {
    }

    public static class Config extends BaseConfig {

        public boolean anvilScreenParticles = true;

        public boolean enchantmentScreenParticles = true;

        public boolean furnaceScreenParticles = true;

        public boolean creativeScreenParticles = true;

        public double creativeScreenParticlesVelX = 0.7d;

        public boolean gameModeSwitcherParticles = true;

        public boolean bundleInputParticles = true;
    }
}
