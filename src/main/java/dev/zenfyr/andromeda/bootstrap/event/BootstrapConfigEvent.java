package dev.zenfyr.andromeda.bootstrap.event;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.config.BootstrapConfig;
import dev.zenfyr.andromeda.bootstrap.event.bus.Bus;

// Executed right after the config for a module is loaded.
public interface BootstrapConfigEvent {

  EventMarker<BootstrapConfigEvent> ID = new EventMarker<>();

  static Bus<BootstrapConfigEvent> get(Module module) {
    return module.getOrCreateBus(
        ID,
        () -> Bus.create(BootstrapConfigEvent.class, events -> config -> {
          for (BootstrapConfigEvent event : events) {
            event.bootstrapConfig(config);
          }
        }));
  }

  void bootstrapConfig(BootstrapConfig config);
}
