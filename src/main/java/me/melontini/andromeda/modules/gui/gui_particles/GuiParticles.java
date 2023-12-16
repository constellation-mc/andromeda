package me.melontini.andromeda.modules.gui.gui_particles;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;

@ModuleTooltip
@ModuleInfo(name = "gui_particles", category = "gui", environment = Environment.CLIENT)
public class GuiParticles extends Module<GuiParticles.Config> {

    public static class Config extends BasicConfig {

        public boolean anvilScreenParticles = true;

        public boolean enchantmentScreenParticles = true;

        public boolean furnaceScreenParticles = true;

        public boolean creativeScreenParticles = true;

        public double creativeScreenParticlesVelX = 0.7d;

        public boolean gameModeSwitcherParticles = true;

        public boolean bundleInputParticles = true;
    }
}
