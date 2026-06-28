package dev.zenfyr.andromeda.modules.entities.minecarts;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.modules.entities.minecarts.client.Client;
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
    public boolean isAnvilMinecartOn = false;

    @ConfigEntry.Gui.RequiresRestart
    public boolean isNoteBlockMinecartOn = false;

    @ConfigEntry.Gui.RequiresRestart
    public boolean isJukeboxMinecartOn = false;

    @ConfigEntry.Gui.RequiresRestart
    public boolean isSpawnerMinecartOn = false;
  }
}
