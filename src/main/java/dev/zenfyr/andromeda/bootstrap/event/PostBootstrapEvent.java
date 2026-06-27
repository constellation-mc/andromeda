package dev.zenfyr.andromeda.bootstrap.event;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.pulsar.api.event.Bus;

// This event should be used to subscribe to non-module events.
// Subscribed to automatically, if a module implements this interface.
// Executed right after all module configs are finished loading
public interface PostBootstrapEvent {

  EventMarker<PostBootstrapEvent> ID = new EventMarker<>();

  static Bus<PostBootstrapEvent> get(Module module) {
    return module.getOrCreateBus(
        ID,
        () -> Bus.create(PostBootstrapEvent.class, events -> () -> {
          for (PostBootstrapEvent event : events) event.postBootstrap();
        }));
  }

  void postBootstrap();
}
