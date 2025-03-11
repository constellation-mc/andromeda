package me.melontini.andromeda.bootstrap.event;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.event.bus.Bus;

// Executed right after all modules finish initializing. (last module's constructor has been called)
public interface PostModuleInitEvent {

  EventMarker<PostModuleInitEvent> ID = new EventMarker<>();

  static Bus<PostModuleInitEvent> get(Module module) {
    return module.getOrCreateBus(
        ID,
        () -> Bus.create(PostModuleInitEvent.class, events -> () -> {
          for (PostModuleInitEvent event : events) event.postModuleInit();
        }));
  }

  void postModuleInit();
}
