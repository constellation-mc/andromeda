package me.melontini.andromeda.common.config;

import lombok.CustomLog;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.util.function.Supplier;

@CustomLog
public class ScopedConfigs {

    public static Supplier<Module.BaseConfig> get(ServerWorld world, Module module) {
        var cd = module.getConfigDefinition(ConfigState.GAME);
        return switch (ModuleManager.get().getConfig(module).scope) {
            case GLOBAL -> () -> Andromeda.GAME_HANDLER.get(cd);
            case WORLD -> () -> ((AttachmentGetter)world.getServer()).andromeda$getConfigs().get(cd);
            case DIMENSION -> () -> ((AttachmentGetter)world).andromeda$getConfigs().get(cd);
        };
    }

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

    public static ConfigHandler getConfigs(ServerWorld world) {
        return ((AttachmentGetter)world).andromeda$getConfigs();
    }

    public interface AttachmentGetter {
        ConfigHandler andromeda$getConfigs();
    }

    public static void init() {
        var manager = ModuleManager.get();

        ServerReloadersEvent.EVENT.register(context -> context.register(new DataConfigs()));

        //ServerLifecycleEvents.SERVER_STARTING.register(server -> DataConfigs.get(server).apply((AttachmentGetter) server));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var list = manager.loaded().stream().filter(module -> manager.getConfig(module).scope.isDimension()).toList();
            server.getWorlds().forEach(world -> manager.cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda"), list));
            manager.cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"),
                    manager.loaded().stream().filter(module -> manager.getConfig(module).scope.isWorld()).toList());
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                var dc = DataConfigs.get(server);
                for (ServerWorld world : server.getWorlds()) dc.apply((AttachmentGetter) world, world.getRegistryKey().getValue());
                dc.apply((AttachmentGetter) server, DataConfigs.DEFAULT);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ServerWorld world : server.getWorlds()) ((ScopedConfigs.AttachmentGetter)world).andromeda$getConfigs().saveAll();
            ((ScopedConfigs.AttachmentGetter)server).andromeda$getConfigs().saveAll();
        });
    }
}
