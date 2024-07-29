package me.melontini.andromeda.modules.items.lockpick;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.number.LongIntermediary;

@ModuleInfo(name = "lockpick", category = "items")
public final class Lockpick extends Module {

  public static final ConfigDefinition<MainConfig> MAIN_CONFIG =
      new ConfigDefinition<>(() -> MainConfig.class);
  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Lockpick() {
    this.defineConfig(ConfigState.MAIN, MAIN_CONFIG);
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this)
        .listen(() -> () -> LockpickItem.init(this, Andromeda.ROOT_HANDLER.get(MAIN_CONFIG)));
    InitEvent.client(this).listen(() -> MerchantInventoryScreen::onClient);
  }

  @ToString
  public static class MainConfig extends BaseConfig {
    public boolean villagerInventory = true;
  }

  @ToString
  public static class Config extends GameConfig {

    public LongIntermediary chance = LongIntermediary.of(3);

    public BooleanIntermediary breakAfterUse = BooleanIntermediary.of(true);
  }
}
