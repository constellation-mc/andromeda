package me.melontini.andromeda.bootstrap.event;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.event.bus.Bus;

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
