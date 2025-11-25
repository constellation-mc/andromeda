package dev.zenfyr.andromeda.modules.items.pouches;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.modules.items.pouches.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleInfo(name = "pouches", category = "items")
public final class Pouches extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> MAIN_CONFIG =
      new ConfigDefinition<>(() -> Config.class);

  Pouches() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> MAIN_CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> Main::init);
    InitEvents.CLIENT.listen(() -> Client::init);
    InitEvents.MERGED.listen(() -> Main::testBlocks);
  }

  public static class Config extends BaseConfig {
    @ConfigEntry.Gui.RequiresRestart
    public boolean seedPouch = true;

    @ConfigEntry.Gui.RequiresRestart
    public boolean flowerPouch = true;

    @ConfigEntry.Gui.RequiresRestart
    public boolean saplingPouch = true;

    @ConfigEntry.Gui.RequiresRestart
    public boolean specialPouch = false;
  }
}
