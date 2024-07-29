package me.melontini.andromeda.base.util.config;

import me.melontini.andromeda.util.CommonValues;
import net.fabricmc.api.EnvType;

public enum ConfigState {
  MAIN(),
  GAME(),
  CLIENT() {
    @Override
    public boolean isAllowed() {
      return CommonValues.environment() == EnvType.CLIENT;
    }
  };

  public boolean isAllowed() {
    return true;
  }
}
