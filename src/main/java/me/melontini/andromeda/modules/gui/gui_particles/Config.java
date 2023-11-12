package me.melontini.andromeda.modules.gui.gui_particles;

import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.util.annotations.config.ValueSwitch;

public class Config extends BasicConfig {

    @ValueSwitch
    public boolean anvilScreenParticles = true;

    @ValueSwitch
    public boolean enchantmentScreenParticles = true;

    @ValueSwitch
    public boolean furnaceScreenParticles = true;

    @ValueSwitch
    public boolean creativeScreenParticles = true;

    @ValueSwitch
    public double creativeScreenParticlesVelX = 0.7d;

    @ValueSwitch
    public boolean gameModeSwitcherParticles = true;
}
