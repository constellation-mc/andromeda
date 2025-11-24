package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.modules.items.pouches.client.Client;
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
