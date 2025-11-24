package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.modules.entities.minecarts.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "minecarts", category = "entities")
public final class Minecarts extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> MAIN_CONFIG =
      new ConfigDefinition<>(() -> Config.class);

  Minecarts() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> MAIN_CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> MinecartItems::init);
    InitEvents.MAIN.listen(() -> MinecartEntities::init);
    InitEvents.CLIENT.listen(() -> Client::init);
  }

  public static final class Config extends BaseConfig {
    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isAnvilMinecartOn = false;

    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isNoteBlockMinecartOn = false;

    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isJukeboxMinecartOn = false;

    @ConfigEntry.Gui.RequiresRestart
    // TODO @SpecialEnvironment(Environment.BOTH)
    public boolean isSpawnerMinecartOn = false;
  }
}
