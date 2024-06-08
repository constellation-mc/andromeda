package me.melontini.andromeda.common.config;

import lombok.CustomLog;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigHandler;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.util.Collections;

@CustomLog
public class ScopedConfigs {

    public interface WorldExtension {
        default Module.BaseConfig am$get(String module) {
            return am$get(ModuleManager.get().getModule(module).orElseThrow(() -> new IllegalStateException("Module %s not found".formatted(module))).getConfigDefinition(ConfigState.GAME));
        }

        default <T extends Module.BaseConfig> T am$get(ConfigDefinition<T> definition) {
            LOGGER.error("Scoped configs requested on client! Returning un-scoped!", AndromedaException.builder()
                    .add("world", ((World)this).getRegistryKey())
                    .build());
            return Andromeda.ROOT_HANDLER.get(definition);
        }

        default boolean am$isReady() {
            return false;
        }
    }

    public interface AttachmentGetter {
        ConfigHandler andromeda$getConfigs();
    }

    public static void init() {
        var manager = ModuleManager.get();

        ServerReloadersEvent.EVENT.register(context -> context.register(new DataConfigs()));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var keep = Experiments.get().persistentScopedConfigs.stream().map(s -> ModuleManager.get().getModule(s).orElseThrow()).toList();
            server.getWorlds().forEach(world -> manager.cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda"), keep));

            manager.cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"), Collections.emptyList());
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (!success) return;

            var dc = DataConfigs.get(server);
            for (ServerWorld world : server.getWorlds()) dc.apply((AttachmentGetter) world, world.getRegistryKey().getValue());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (Experiments.get().persistentScopedConfigs.isEmpty()) return;

            for (ServerWorld world : server.getWorlds()) {
                var attachment = ((ScopedConfigs.AttachmentGetter) world).andromeda$getConfigs();
                for (String id : Experiments.get().persistentScopedConfigs) {
                    attachment.save(manager.getModule(id).orElseThrow(() -> new RuntimeException("No such module %s!".formatted(id))));
                }
            }
        });
    }
}
