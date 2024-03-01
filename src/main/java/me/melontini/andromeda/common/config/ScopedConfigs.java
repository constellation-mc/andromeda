package me.melontini.andromeda.common.config;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.CustomLog;
import lombok.SneakyThrows;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@CustomLog
public class ScopedConfigs {

    public static <T extends Module.BaseConfig> T get(World world, Module<T> module) {
        if (world instanceof ServerWorld sw) {
            return switch (module.config().scope) {
                case GLOBAL -> module.config();
                case WORLD -> getConfigs(sw.getServer().getWorld(World.OVERWORLD)).get(module);
                case DIMENSION -> getConfigs(sw).get(module);
            };
        }
        LOGGER.error("Scoped configs requested on client! Returning un-scoped!", AndromedaException.builder()
                .add("module", module.meta().id())
                .add("world", world.getRegistryKey())
                .build());
        return module.config();
    }

    public static Path getPath(World world, Module<?> m) {
        if (world instanceof ServerWorld w) {
            return switch (m.config().scope) {
                case GLOBAL -> FabricLoader.getInstance().getConfigDir();
                case WORLD -> w.getServer().session.getDirectory(WorldSavePath.ROOT).resolve("config");
                case DIMENSION ->
                        w.getServer().session.getWorldDirectory(world.getRegistryKey()).resolve("world_config");
            };
        }
        throw new IllegalStateException();
    }

    @SneakyThrows
    private static Module.BaseConfig loadScoped(Path root, Module<?> module) {
        var manager = module.manager();
        if (Files.exists(manager.resolve(root))) {
            return manager.load(root);
        }
        return manager.load(FabricLoader.getInstance().getConfigDir());
    }

    static void prepareForWorld(ServerWorld world, Module<?> module, Path p) {
        Attachment attachment = ScopedConfigs.getConfigs(world);
        Module.BaseConfig config = ScopedConfigs.loadScoped(p, module);

        module.manager().save(p, Utilities.cast(config));

        if (module.config().scope == Module.BaseConfig.Scope.DIMENSION) {
            DataConfigs.applyDataPacks(config, module, world.getRegistryKey().getValue());
        }
        attachment.addConfig(module, config);
    }

    public interface WorldExtension {
        default <T extends Module.BaseConfig> T am$get(Class<? extends Module<T>> cls) {
            return am$get(ModuleManager.quick(cls));
        }

        default Module.BaseConfig am$get(String module) {
            return am$get(ModuleManager.get().getModule(module).orElseThrow(() -> new IllegalStateException("Module %s not found".formatted(module))));
        }

        default <T extends Module.BaseConfig> T am$get(Module<T> module) {
            return ScopedConfigs.get((World) this, module);
        }

        default <T extends Module.BaseConfig> void am$save(Class<? extends Module<T>> cls) {
            am$save(ModuleManager.quick(cls));
        }

        default void am$save(String module) {
            am$save(ModuleManager.get().getModule(module).orElseThrow(() -> new IllegalStateException("Module %s not found".formatted(module))));
        }

        default <T extends Module.BaseConfig> void am$save(Module<T> module) {
            if (this instanceof ServerWorld w) {
                module.manager().save(getPath(w, module), am$get(module));
            }
        }

        default boolean am$isReady() {
            return this instanceof ServerWorld;
        }
    }

    public static Attachment getConfigs(ServerWorld world) {
        return ((AttachmentGetter)world).andromeda$getConfigs();
    }

    public interface AttachmentGetter {
        Attachment andromeda$getConfigs();
    }

    public static class Attachment {

        private final Map<Module<?>, Module.BaseConfig> configs = new Reference2ObjectOpenHashMap<>();

        public <T extends Module.BaseConfig> T get(Module<T> module) {
            return (T) configs.get(module);
        }

        public void addConfig(Module<?> module, Module.BaseConfig config) {
            synchronized (this.configs) {
                this.configs.put(module, config);
            }
        }
    }
}
