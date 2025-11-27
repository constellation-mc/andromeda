package dev.zenfyr.andromeda.common.client;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.config.handler.MultiConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class AndromedaClient implements ClientModInitializer {

  private static AndromedaClient instance;

  public static final MultiConfigHandler CLIENT = new MultiConfigHandler(
      ModuleManager.get(),
      FabricLoader.getInstance().getConfigDir(),
      "client",
      RegisterConfigEvent.CLIENT);

  @Override
  public void onInitializeClient() {
    Andromeda.get().onMergedEntryPoint();
    instance = this;

    var manager = ModuleManager.get();

    CLIENT.loadAll();
    CLIENT.saveAll();

    InitEvents.CLIENT.invoker().onModuleClientInit().runEntrypoint();
  }

  public static AndromedaClient get() {
    return instance;
  }
}
