package dev.zenfyr.andromeda.bootstrap.event;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.pulsar.api.event.Bus;

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
