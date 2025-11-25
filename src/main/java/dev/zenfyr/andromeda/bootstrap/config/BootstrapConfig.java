package dev.zenfyr.andromeda.bootstrap.config;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.event.CreateBootstrapConfigEvent;

public class BootstrapConfig extends BaseConfig {

  public boolean enabled = false;

  public static BootstrapConfig create(Module module) {
    var config = new BootstrapConfig();
    var bus = module.getOrCreateBus(CreateBootstrapConfigEvent.ID, null);
    if (bus != null) config.enabled = bus.invoker().createBootstrapConfig();
    return config;
  }
}
