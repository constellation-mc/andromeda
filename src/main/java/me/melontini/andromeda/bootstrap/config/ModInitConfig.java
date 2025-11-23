package me.melontini.andromeda.bootstrap.config;

import me.melontini.andromeda.bootstrap.ModuleManager;

public class ModInitConfig extends BaseConfig {

  public static final ModConfigHandler.Key<ModInitConfig> KEY =
      new ModConfigHandler.Key<>("init", ModInitConfig.class);

  public byte sixSeven = 67;

  public static ModInitConfig get() {
    return ModuleManager.get().modConfig().get(KEY);
  }
}
