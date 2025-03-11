package me.melontini.andromeda.bootstrap.config;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.event.CreateBootstrapConfigEvent;

public class BootstrapConfig extends BaseConfig {

  public boolean enabled = false;

  public static BootstrapConfig create(Module module) {
    var config = new BootstrapConfig();
    var bus = module.getOrCreateBus(CreateBootstrapConfigEvent.ID, null);
    if (bus != null) config.enabled = bus.invoker().createBootstrapConfig();
    return config;
  }
}
