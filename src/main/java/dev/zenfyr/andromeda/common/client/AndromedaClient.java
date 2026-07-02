package dev.zenfyr.andromeda.common.client;

import com.mojang.blaze3d.vertex.*;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.config.handler.MultiConfigHandler;
import dev.zenfyr.pulsar.api.platform.Platform;
import net.fabricmc.api.ClientModInitializer;

public class AndromedaClient implements ClientModInitializer {

  private static AndromedaClient instance;

  public static final MultiConfigHandler CLIENT = new MultiConfigHandler(
      ModuleManager.get(),
      Platform.getPlatform().getConfigDir(),
      "client",
      RegisterConfigEvent.CLIENT);

  @Override
  public void onInitializeClient() {
    var manager = ModuleManager.get();
    Andromeda.get().onMergedEntryPoint(manager);
    instance = this;

    CLIENT.loadAll();
    CLIENT.saveAll();

    InitEvents.CLIENT.invoker().onModuleClientInit().runEntrypoint();
  }

  public static AndromedaClient get() {
    return instance;
  }
}
