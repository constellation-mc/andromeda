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
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.andromeda.util.exceptions.AndromedaException;
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

import static me.melontini.andromeda.util.CommonValues.MODID;

public class DataConfigs extends JsonDataLoader {

    private static final Identifier DEFAULT = new Identifier(MODID, "default");
    public static final Identifier RELOADER_ID = Common.id("scoped_config");

    public DataConfigs() {
        super(RELOADER_ID);
    }

    public static DataConfigs get(MinecraftServer server) {
        try {
            return server.am$getReloader(RELOADER_ID);
        } catch (NullPointerException e) {
            throw AndromedaException.builder().cause(e).report(false)
                    .message("Couldn't get Scoped Configs reloader! Have you restarted the game as you were asked to?")
                    .build();
        }
    }

    public Map<Identifier, Map<Module<?>, Set<Data>>> configs;
    public Map<Module<?>, Set<Data>> defaultConfigs;

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        if (!Experiments.get().scopedConfigs) {
            return;
        }

        Map<Identifier, Map<Module<?>, Set<CompletableFuture<Data>>>> configs = new Object2ObjectOpenHashMap<>();
        data.forEach((id, element) -> {
            JsonObject object = element.getAsJsonObject();

            var m = ModuleManager.get().getModule(id.getPath()).orElseThrow(() -> new IllegalStateException("Invalid module path '%s'! The module must be enabled!".formatted(id.getPath())));
            var cls = ModuleManager.get().getConfigClass(m.getClass());

            if (m.config().scope.isWorld()) {
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

        Map<Identifier, Map<Module<?>, Set<Data>>> parsed = new Object2ObjectOpenHashMap<>();
        CompletableFuture.allOf(configs.values().stream().flatMap(map -> map.values().stream())
                .flatMap(Collection::stream).toArray(CompletableFuture[]::new)).handle((unused, throwable) -> configs).join().forEach((identifier, moduleSetMap) -> {
            var n = parsed.computeIfAbsent(identifier, id -> new Object2ObjectOpenHashMap<>());
            moduleSetMap.forEach((module, completableFutures) -> {
                var set = n.computeIfAbsent(module, m -> new ReferenceLinkedOpenHashSet<>());
                completableFutures.forEach(future -> set.add(future.join()));
            });
        });
        defaultConfigs = parsed.remove(DEFAULT);
        this.configs = parsed;
    }

    private CompletableFuture<Data> makeFuture(Gson gson, Module<?> m, Class<? extends Module.BaseConfig> cls, JsonElement element) {
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

    public void apply(ServerWorld world) {
        if (!Experiments.get().scopedConfigs) return;

        MakeSure.notNull(configs);
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

    public void apply(MinecraftServer server) {
        if (!Experiments.get().scopedConfigs) return;

        MakeSure.notNull(configs);

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

    private void apply(Module.BaseConfig config, Data data) {
        data.fields().forEach((field) -> {
            try {
                field.set(config, field.get(data.config()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to apply config data for module '%s'".formatted(config.getClass().getSimpleName()), e);
            }
        });
    }

    void applyDataPacks(Module.BaseConfig config, Module<?> m, Identifier id) {
        if (defaultConfigs != null) {
            var forModule = defaultConfigs.get(m);
            if (forModule != null) {
                for (Data tuple : forModule) apply(config, tuple);
            }
        }

        var data = configs.get(id);
        if (data != null) {
            var forModule = data.get(m);
            if (forModule != null) {
                for (Data tuple : forModule) apply(config, tuple);
            }
        }
    }
}
