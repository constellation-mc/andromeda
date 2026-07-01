package dev.zenfyr.andromeda.bootstrap.util;

import dev.zenfyr.pulsar.api.platform.CEnvType;

public enum Environment {
  CLIENT,
  SERVER,
  BOTH {
    @Override
    public boolean allows(Environment environment) {
      return true;
    }
  },
  ANY {
    @Override
    public boolean allows(Environment environment) {
      return true;
    }
  };

  public boolean allows(CEnvType envType) {
    return allows(envType == CEnvType.CLIENT ? CLIENT : SERVER);
  }

  public boolean allows(Environment environment) {
    return this == environment;
  }

  public boolean isClient() {
    return this == CLIENT;
  }

  public boolean isServer() {
    return this == SERVER;
  }

  public boolean isAny() {
    return this == ANY;
  }

  public boolean isBoth() {
    return this == BOTH;
  }
}
