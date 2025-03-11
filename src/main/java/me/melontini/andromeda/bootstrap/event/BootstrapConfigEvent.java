package me.melontini.andromeda.bootstrap.event;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.config.BootstrapConfig;
import me.melontini.andromeda.bootstrap.event.bus.Bus;

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
