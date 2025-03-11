package me.melontini.andromeda.bootstrap.config;

import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.bootstrap.config.handler.ModConfigHandler;

public class ModInitConfig extends BaseConfig {

  public static final ModConfigHandler.Key<ModInitConfig> KEY =
      new ModConfigHandler.Key<>("init", ModInitConfig.class);

  public boolean sideOnlyMode = false;

  public static ModInitConfig get() {
    return ModuleManager.get().modConfig().get(KEY);
  }
}
