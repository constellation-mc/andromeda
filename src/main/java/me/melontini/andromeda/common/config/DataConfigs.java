package me.melontini.andromeda.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonDataLoader;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class DataConfigs extends JsonDataLoader {
    public DataConfigs() {
        super(new Gson(), "andromeda/scoped_config");
    }

    public static Map<Identifier, Map<Module<?>, Set<Tuple<Set<Field>, ? extends BasicConfig>>>> CONFIGS;

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, Map<Module<?>, Set<CompletableFuture<Tuple<Set<Field>, ? extends BasicConfig>>>>> configs = new HashMap<>();
            data.forEach((id, object) -> {
                var m = ModuleManager.get().getModule(id.getPath()).orElseThrow(() -> new IllegalStateException("Invalid module path '%s'!".formatted(id.getPath())));
                if (m.config().scope != BasicConfig.Scope.DIMENSION)
                    throw new IllegalStateException("Invalid module scope `%s` for '%s'! Must be '%s'".formatted(m.config().scope, m.meta().id(), BasicConfig.Scope.DIMENSION));

                var cls = ModuleManager.get().getConfigClass(m.getClass());
                object.entrySet().forEach(entry -> {
                    var map = configs.computeIfAbsent(Identifier.tryParse(entry.getKey()), string -> new HashMap<>());
                    map.computeIfAbsent(m, module -> new LinkedHashSet<>()).add(CompletableFuture.supplyAsync(() -> {
                        var instance = this.gson.fromJson(entry.getValue(), cls);
                        Set<Field> config = new HashSet<>();
                        entry.getValue().getAsJsonObject().entrySet().forEach(entry2 -> {
                            try {
                                config.add(cls.getField(entry2.getKey()));
                            } catch (NoSuchFieldException e) {
                                throw new CompletionException("Failed to load config data for module '%s'".formatted(m.meta().id()), e);
                            }
                        });
                        return Tuple.of(config, instance);
                    }, executor));
                });
            });
            return CompletableFuture.allOf(configs.values().stream().flatMap(map -> map.values().stream())
                    .flatMap(Collection::stream).toArray(CompletableFuture[]::new)).handle((unused, throwable) -> configs);
        }, executor).thenAcceptAsync(map -> {
            var p = Utilities.supplyUnchecked(map::get);
            Map<Identifier, Map<Module<?>, Set<Tuple<Set<Field>, ? extends BasicConfig>>>> configs = new HashMap<>();
            p.forEach((identifier, moduleSetMap) -> {
                var n = configs.computeIfAbsent(identifier, id -> new HashMap<>());
                moduleSetMap.forEach((module, completableFutures) -> {
                    var set = n.computeIfAbsent(module, m -> new LinkedHashSet<>());
                    completableFutures.forEach(future -> set.add(future.join()));
                });
            });
            CONFIGS = configs;
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return Common.id("data_configs");
    }

    public static void apply(MinecraftServer server) {
        MakeSure.notNull(DataConfigs.CONFIGS);

        server.getWorlds().forEach(world -> ModuleManager.get().cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda")));
        ModuleManager.get().cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"));

        server.getWorlds().forEach(ScopedConfigs::get);

        Set<CompletableFuture<?>> futures = new HashSet<>();
        for (Module<?> module : ModuleManager.get().all()) {
            if (module.meta().environment() == Environment.CLIENT) continue; //Those are always GLOBAL.

            switch (module.config().scope) {
                case WORLD -> futures.add(CompletableFuture.runAsync(() -> {
                    ServerWorld world = server.getWorld(World.OVERWORLD);
                    Path p = server.session.getDirectory(WorldSavePath.ROOT).resolve("config");
                    prepareForWorld(world, module, p);
                }));
                case DIMENSION -> CompletableFuture.runAsync(() -> {
                    for (ServerWorld world : server.getWorlds()) {
                        Path p = server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config");
                        prepareForWorld(world, module, p);
                    }
                });
            }
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }

    @SneakyThrows
    private static BasicConfig loadScoped(Path root, Module<?> module) {
        var manager = module.manager();
        if (Files.exists(manager.resolve(root))) {
            return manager.load(root);
        }
        return manager.load(FabricLoader.getInstance().getConfigDir());
    }

    private static void applyDataPacks(BasicConfig config, Module<?> m, Identifier id) {
        var data = DataConfigs.CONFIGS.get(id);
        if (data != null) {
            var forModule = data.get(m);
            if (forModule != null) {
                for (Tuple<Set<Field>, ? extends BasicConfig> tuple : forModule) {
                    tuple.left().forEach((field) -> {
                        try {
                            field.set(config, field.get(tuple.right()));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Failed to apply config data for module '%s'".formatted(m.meta().id()), e);
                        }
                    });
                }
            }
        }
    }

    private static void prepareForWorld(ServerWorld world, Module<?> module, Path p) {
        ScopedConfigs.State state = ScopedConfigs.get(world);
        BasicConfig config = loadScoped(p, module);

        if (module.config().scope == BasicConfig.Scope.DIMENSION) {
            applyDataPacks(config, module, world.getRegistryKey().getValue());
        }
        state.addConfig(module, config);
        try {
            module.manager().save(p, Utilities.cast(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
