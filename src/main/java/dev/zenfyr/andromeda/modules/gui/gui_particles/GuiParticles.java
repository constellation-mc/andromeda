package dev.zenfyr.andromeda.modules.gui.gui_particles;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "gui_particles", category = "gui", env = Environment.CLIENT)
public final class GuiParticles extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GuiParticles() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.CLIENT).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.CLIENT.listen(() -> GuiParticlesClient::init);
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
