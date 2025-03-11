package me.melontini.andromeda.common;

import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import net.fabricmc.api.DedicatedServerModInitializer;

public class AndromedaServer implements DedicatedServerModInitializer {

  @Override
  public void onInitializeServer() {
    Andromeda.get().onMergedEntryPoint();

    var manager = ModuleManager.get();
    InitEvents.SERVER.invoker().onModuleServerInit().runEntrypoint();
  }
}
