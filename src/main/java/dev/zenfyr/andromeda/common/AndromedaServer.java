package dev.zenfyr.andromeda.common;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import net.fabricmc.api.DedicatedServerModInitializer;

public class AndromedaServer implements DedicatedServerModInitializer {

  @Override
  public void onInitializeServer() {
    var manager = ModuleManager.get();

    Andromeda.get().onMergedEntryPoint(manager);
    InitEvents.SERVER.invoker().onModuleServerInit().runEntrypoint();
  }
}
