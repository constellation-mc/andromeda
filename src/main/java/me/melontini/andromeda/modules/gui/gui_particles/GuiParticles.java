package me.melontini.andromeda.modules.gui.gui_particles;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.modules.gui.gui_particles.client.Client;

import java.util.List;

@ModuleInfo(name = "gui_particles", category = "gui", environment = Environment.CLIENT)
public final class GuiParticles extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    GuiParticles() {
        this.defineConfig(ConfigState.CLIENT, CONFIG);
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    @ToString
    public static final class Config extends BaseConfig {

        public boolean anvilScreenParticles = true;

        public boolean enchantmentScreenParticles = true;

        public boolean furnaceScreenParticles = true;

        public boolean creativeScreenParticles = true;

        public double creativeScreenParticlesVelX = 0.7d;

        public boolean gameModeSwitcherParticles = true;

        public boolean bundleInputParticles = true;
    }
}
