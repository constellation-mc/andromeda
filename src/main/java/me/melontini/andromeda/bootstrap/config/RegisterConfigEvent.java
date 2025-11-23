package me.melontini.andromeda.bootstrap.config;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.event.EventMarker;
import me.melontini.andromeda.bootstrap.event.bus.Bus;

public interface RegisterConfigEvent {

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
}
