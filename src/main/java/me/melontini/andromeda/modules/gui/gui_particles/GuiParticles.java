package me.melontini.andromeda.modules.gui.gui_particles;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "gui_particles", category = "gui", env = Environment.CLIENT)
public final class GuiParticles extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GuiParticles() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.CLIENT).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.CLIENT.listen(() -> Client::init);
  }

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
