package dev.zenfyr.andromeda.modules.items.lockpick;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.common.config.GameConfig;

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
