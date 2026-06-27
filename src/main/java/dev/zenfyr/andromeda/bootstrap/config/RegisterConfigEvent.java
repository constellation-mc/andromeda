package dev.zenfyr.andromeda.bootstrap.config;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.event.EventMarker;
import dev.zenfyr.pulsar.api.event.Bus;

public interface RegisterConfigEvent {

  EventMarker<RegisterConfigEvent> CLIENT = new EventMarker<>();
  EventMarker<RegisterConfigEvent> MAIN = new EventMarker<>();
  // The Game
  EventMarker<RegisterConfigEvent> GAME = new EventMarker<>();

  static Bus<RegisterConfigEvent> get(Module module, EventMarker<RegisterConfigEvent> id) {
    return module.getOrCreateBus(
        id,
        () -> Bus.create(RegisterConfigEvent.class, events -> () -> {
          for (RegisterConfigEvent event : events) {
            var def = event.onRegisterConfigs();
            if (def != null) return def;
          }
          return null;
        }));
  }

  ConfigDefinition<?> onRegisterConfigs();

  interface ConfigRegistrar {
    void accept(ConfigDefinition<?> definition);
  }
}
