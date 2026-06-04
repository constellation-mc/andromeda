package dev.zenfyr.andromeda.bootstrap.config;

import dev.zenfyr.andromeda.bootstrap.config.handler.ModConfigHandler;

public class ModInitConfig extends BaseConfig {

  public static final ModConfigHandler.Key<ModInitConfig> KEY =
      new ModConfigHandler.Key<>("init", ModInitConfig.class);

  public boolean sideOnlyMode = false;
}
