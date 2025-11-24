package me.melontini.andromeda.bootstrap.event;

import me.melontini.andromeda.api.ApiDeclaration;
import me.melontini.andromeda.bootstrap.event.bus.Bus;

// Event to declare module APIs. The API system is not very good.
// Must be invoked in the module constructor.
public interface DeclareApiEvent {

  Bus<DeclareApiEvent> BUS = Bus.create(DeclareApiEvent.class, events -> consumer -> {
    for (DeclareApiEvent event : events) {
      event.declareModuleApi(consumer);
    }
  });

  void declareModuleApi(ModuleApiDeclarinator consumer);

  interface ModuleApiDeclarinator {
    <I, O> void accept(ApiDeclaration<I, O> declaration);
  }
}
