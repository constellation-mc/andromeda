package me.melontini.andromeda.common.config;

import lombok.CustomLog;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

@CustomLog
public class ScopedConfigs {

    public static <T extends Module.BaseConfig> ConfigHandler.Entry<T> get(World world, Module<T> module) {
        if (world instanceof ServerWorld sw) {
            return switch (Andromeda.getConfig(module).e.scope) {
                case GLOBAL -> Andromeda.getConfig(module);
                case WORLD -> ((AttachmentGetter)sw.getServer()).andromeda$getConfigs().get(module);
                case DIMENSION -> ((AttachmentGetter)sw).andromeda$getConfigs().get(module);
            };
        }
        LOGGER.error("Scoped configs requested on client! Returning un-scoped!", AndromedaException.builder()
                .add("module", module.meta().id())
                .add("world", world.getRegistryKey())
                .build());
        return Andromeda.getConfig(module);
    }

    public interface WorldExtension {
        default <T extends Module.BaseConfig> ConfigHandler.Entry<T> am$get(Class<? extends Module<T>> cls) {
            return am$get(ModuleManager.quick(cls));
        }

        default ConfigHandler.Entry<?> am$get(String module) {
            return am$get(ModuleManager.get().getModule(module).orElseThrow(() -> new IllegalStateException("Module %s not found".formatted(module))));
        }

        default <T extends Module.BaseConfig> ConfigHandler.Entry<T> am$get(Module<T> module) {
            return ScopedConfigs.get((World) this, module);
        }

        default boolean am$isReady() {
            return this instanceof ServerWorld;
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
            var list = manager.loaded().stream().filter(module -> Andromeda.getConfig(module).e.scope.isDimension()).toList();
            server.getWorlds().forEach(world -> manager.cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda"), list));
            manager.cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"),
                    manager.loaded().stream().filter(module -> Andromeda.getConfig(module).e.scope.isWorld()).toList());
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
