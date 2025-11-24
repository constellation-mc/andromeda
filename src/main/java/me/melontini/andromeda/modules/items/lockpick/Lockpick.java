package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "lockpick", category = "items")
public final class Lockpick extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<MainConfig> MAIN_CONFIG =
      new ConfigDefinition<>(() -> MainConfig.class);
  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Lockpick() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> MAIN_CONFIG);
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> LockpickItem::init);
    InitEvents.CLIENT.listen(() -> MerchantInventoryScreen::onClient);
  }

  public static class MainConfig extends BaseConfig {
    public boolean villagerInventory = true;
  }

  public static class Config extends GameConfig {

    public int chance = 3;

    public boolean breakAfterUse = true;
  }
}
