package me.melontini.andromeda.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class DataConfigs extends JsonDataLoader {
    public DataConfigs() {
        super(new Gson(), "andromeda/scoped_config");
    }

    public static Map<Identifier, Map<Module<?>, Set<Data>>> CONFIGS;
    public static Map<Module<?>, Set<Data>> DEFAULT_CONFIGS;
    private static final Identifier DEFAULT = new Identifier(MODID, "default");

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, Map<Module<?>, Set<CompletableFuture<Data>>>> configs = new Object2ObjectOpenHashMap<>();
            data.forEach((id, object) -> {
                var m = ModuleManager.get().getModule(id.getPath()).orElseThrow(() -> new IllegalStateException("Invalid module path '%s'! The module must be enabled!".formatted(id.getPath())));
                var cls = ModuleManager.get().getConfigClass(m.getClass());

                if (m.config().scope == Module.BaseConfig.Scope.WORLD) {
                    if (!object.has(DEFAULT.toString()) || object.size() > 1)
                        throw new IllegalStateException("'%s' modules only support '%s' as their dimension!".formatted(Module.BaseConfig.Scope.WORLD, DEFAULT));

                    var map = configs.computeIfAbsent(DEFAULT, identifier -> new Reference2ObjectOpenHashMap<>());
                    map.computeIfAbsent(m, module -> new ReferenceLinkedOpenHashSet<>())
                            .add(makeFuture(this.gson, m, cls, object.get(DEFAULT.toString())));
                } else {
                    object.entrySet().forEach(entry -> {
                        var map = configs.computeIfAbsent(Identifier.tryParse(entry.getKey()), string -> new Reference2ObjectOpenHashMap<>());
                        map.computeIfAbsent(m, module -> new ReferenceLinkedOpenHashSet<>())
                                .add(makeFuture(this.gson, m, cls, entry.getValue()));
                    });
                }
            });
            return CompletableFuture.allOf(configs.values().stream().flatMap(map -> map.values().stream())
                    .flatMap(Collection::stream).toArray(CompletableFuture[]::new)).handle((unused, throwable) -> configs);
        }, executor).thenAcceptAsync(map -> {
            Map<Identifier, Map<Module<?>, Set<Data>>> configs = new Object2ObjectOpenHashMap<>();
            map.join().forEach((identifier, moduleSetMap) -> {
                var n = configs.computeIfAbsent(identifier, id -> new Object2ObjectOpenHashMap<>());
                moduleSetMap.forEach((module, completableFutures) -> {
                    var set = n.computeIfAbsent(module, m -> new ReferenceLinkedOpenHashSet<>());
                    completableFutures.forEach(future -> set.add(future.join()));
                });
            });
            DEFAULT_CONFIGS = configs.get(DEFAULT);
            configs.remove(DEFAULT);
            CONFIGS = configs;
        }, executor);
    }

    private static CompletableFuture<Data> makeFuture(Gson gson, Module<?> m, Class<? extends Module.BaseConfig> cls, JsonElement element) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var instance = gson.fromJson(element, cls);
                Set<Field> config = new ReferenceOpenHashSet<>();
                element.getAsJsonObject().entrySet().forEach(entry2 -> {
                    try {
                        var f = cls.getField(entry2.getKey());
                        if (f.isAnnotationPresent(Unscoped.class))
                            throw new IllegalStateException("Attempted to modify an unscoped field '%s'!".formatted(entry2.getKey()));
                        config.add(f);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException("Failed to load config data for module '%s'".formatted(m.meta().id()), e);
                    }
                });
                return new Data(config, instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load config data for module '%s'".formatted(m.meta().id()), e);
            }
        }, Util.getMainWorkerExecutor());
    }

    public record Data(Set<Field> fields, Module.BaseConfig config) {
    }

    @Override
    public Identifier getFabricId() {
        return Common.id("data_configs");
    }

    public static void apply(ServerWorld world) {
        MakeSure.notNull(DataConfigs.CONFIGS);
        ScopedConfigs.getConfigs(world);

        Set<CompletableFuture<?>> futures = new ReferenceOpenHashSet<>();
        for (Module<?> module : ModuleManager.get().loaded()) {
            switch (module.config().scope) {
                case WORLD -> futures.add(CompletableFuture.runAsync(() -> {
                    if (world.getRegistryKey().equals(World.OVERWORLD))
                        ScopedConfigs.prepareForWorld(world, module, ScopedConfigs.getPath(world, module));
                }, Util.getMainWorkerExecutor()));
                case DIMENSION ->
                        CompletableFuture.runAsync(() -> ScopedConfigs.prepareForWorld(world, module, ScopedConfigs.getPath(world, module)), Util.getMainWorkerExecutor());
            }
        }
        var task = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        world.getServer().runTasks(task::isDone);
    }

    public static void apply(MinecraftServer server) {
        MakeSure.notNull(DataConfigs.CONFIGS);

        server.getWorlds().forEach(ScopedConfigs::getConfigs);

        Set<CompletableFuture<?>> futures = new ReferenceOpenHashSet<>();
        for (Module<?> module : ModuleManager.get().loaded()) {
            switch (module.config().scope) {
                case WORLD -> futures.add(CompletableFuture.runAsync(() -> {
                    ServerWorld world = server.getWorld(World.OVERWORLD);
                    ScopedConfigs.prepareForWorld(world, module, ScopedConfigs.getPath(world, module));
                }, Util.getMainWorkerExecutor()));
                case DIMENSION -> CompletableFuture.runAsync(() -> {
                    for (ServerWorld world : server.getWorlds()) {
                        ScopedConfigs.prepareForWorld(world, module, ScopedConfigs.getPath(world, module));
                    }
                }, Util.getMainWorkerExecutor());
            }
        }
        var task = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        server.runTasks(task::isDone);
    }

    private static void apply(Module.BaseConfig config, Data data) {
        data.fields().forEach((field) -> {
            try {
                field.set(config, field.get(data.config()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to apply config data for module '%s'".formatted(config.getClass().getSimpleName()), e);
            }
        });
    }

    static void applyDataPacks(Module.BaseConfig config, Module<?> m, Identifier id) {
        if (DEFAULT_CONFIGS != null) {
            var forModule = DEFAULT_CONFIGS.get(m);
            if (forModule != null) {
                for (Data tuple : forModule) apply(config, tuple);
            }
        }

        var data = DataConfigs.CONFIGS.get(id);
        if (data != null) {
            var forModule = data.get(m);
            if (forModule != null) {
                for (Data tuple : forModule) apply(config, tuple);
            }
        }
    }
}
