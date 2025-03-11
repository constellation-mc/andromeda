package me.melontini.andromeda.bootstrap.event;

import me.melontini.andromeda.bootstrap.event.bus.Bus;

public final class InitEvents {

  public static final Bus<ModuleMainInit> MAIN = Bus.create(
      ModuleMainInit.class,
      events -> () -> () -> {
        for (ModuleMainInit event : events) event.onModuleMainInit().runEntrypoint();
      });
  public static final Bus<ModuleMergedInit> MERGED = Bus.create(
      ModuleMergedInit.class,
      events -> () -> () -> {
        for (ModuleMergedInit event : events) event.onModuleMergedInit().runEntrypoint();
      });
  public static final Bus<ModuleClientInit> CLIENT = Bus.create(
      ModuleClientInit.class,
      events -> () -> () -> {
        for (ModuleClientInit event : events) event.onModuleClientInit().runEntrypoint();
      });
  public static final Bus<ModuleServerInit> SERVER = Bus.create(
      ModuleServerInit.class,
      events -> () -> () -> {
        for (ModuleServerInit event : events) event.onModuleServerInit().runEntrypoint();
      });

  public interface ModuleClientInit {
    ManagerConsumer onModuleClientInit();
  }

  public interface ModuleServerInit {
    ManagerConsumer onModuleServerInit();
  }

  public interface ModuleMergedInit {
    ManagerConsumer onModuleMergedInit();
  }

  public interface ModuleMainInit {
    ManagerConsumer onModuleMainInit();
  }

  public interface ManagerConsumer {
    void runEntrypoint();
  }
}
