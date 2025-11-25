package dev.zenfyr.andromeda.bootstrap.event;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.event.bus.Bus;

// Executed when the BCH needs to create a fresh config. Use to enable configs by default.
public interface CreateBootstrapConfigEvent {

  EventMarker<CreateBootstrapConfigEvent> ID = new EventMarker<>();

  static Bus<CreateBootstrapConfigEvent> get(Module module) {
    return module.getOrCreateBus(
        ID,
        () -> Bus.create(CreateBootstrapConfigEvent.class, events -> () -> {
          boolean result = false;
          for (CreateBootstrapConfigEvent event : events) {
            result |= event.createBootstrapConfig();
          }
          return result;
        }));
  }

  boolean createBootstrapConfig();
}
