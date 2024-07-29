package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

public interface BeforeDataPackSyncEvent {

  Event<BeforeDataPackSyncEvent> EVENT =
      EventFactory.createArrayBacked(BeforeDataPackSyncEvent.class, afterFirstReload -> server -> {
        for (BeforeDataPackSyncEvent event : afterFirstReload) {
          event.beforeDataPackReload(server);
        }
      });

  void beforeDataPackReload(MinecraftServer server);
}
