package me.melontini.andromeda.bootstrap.event;

import me.melontini.andromeda.bootstrap.event.bus.Bus;

public final class InitEvents {

  public static final Bus<ModuleMainInit> MAIN = Bus.create(
      ModuleMainInit.class,
      events -> () -> () -> {
        for (ModuleMainInit event : events) event.onModuleMainInit().runEntrypoint();
      });
  public static final Bus<ModuleServerInit> SERVER = Bus.create(
      ModuleServerInit.class,
      events -> () -> () -> {
        for (ModuleServerInit event : events) event.onModuleServerInit().runEntrypoint();
      });

  public interface ModuleServerInit {
    ManagerConsumer onModuleServerInit();
  }

  public interface ModuleMainInit {
    ManagerConsumer onModuleMainInit();
  }

  public interface ManagerConsumer {
    void runEntrypoint();
  }
}
