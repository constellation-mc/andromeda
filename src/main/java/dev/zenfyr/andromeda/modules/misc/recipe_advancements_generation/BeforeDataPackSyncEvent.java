package dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation;

import dev.zenfyr.pulsar.api.event.Bus;
import net.minecraft.server.MinecraftServer;

public interface BeforeDataPackSyncEvent {

  Bus<BeforeDataPackSyncEvent> EVENT =
      Bus.create(BeforeDataPackSyncEvent.class, afterFirstReload -> server -> {
        for (BeforeDataPackSyncEvent event : afterFirstReload) {
          event.beforeDataPackReload(server);
        }
      });

  void beforeDataPackReload(MinecraftServer server);
}
