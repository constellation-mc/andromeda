package me.melontini.andromeda.modules.gui.gui_particles;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.client.AndromedaClient;


@ModuleInfo(name = "gui_particles", category = "gui", environment = Environment.CLIENT)
public final class GuiParticles extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    GuiParticles() {
        this.defineConfig(ConfigState.CLIENT, CONFIG);
        InitEvent.client(this).listen(() -> () -> Client.init(AndromedaClient.HANDLER.get(CONFIG)));
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
