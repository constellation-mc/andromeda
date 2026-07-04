package dev.zenfyr.andromeda.bootstrap.event;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.pulsar.api.event.Bus;

public interface ModuleLoadStateEvent {

  EventMarker<ModuleLoadStateEvent> ID = new EventMarker<>();

  Result DEFAULT = new Result(ForcedState.NONE, null);

  static Bus<ModuleLoadStateEvent> get(Module module) {
    return module.getOrCreateBus(
        ID,
        () -> Bus.create(ModuleLoadStateEvent.class, events -> () -> {
          for (ModuleLoadStateEvent event : events) {
            var result = event.onModuleLoadState();
            if (result.state() != ForcedState.NONE) return result;
          }
          return DEFAULT;
        }));
  }

  Result onModuleLoadState();

  static Result disable(String msg) {
    return new Result(ForcedState.DISABLE, msg);
  }

  static Result enable(String msg) {
    return new Result(ForcedState.ENABLE, msg);
  }

  enum ForcedState {
    NONE,
    ENABLE,
    DISABLE
  }

  record Result(ForcedState state, String msg) {}
}
