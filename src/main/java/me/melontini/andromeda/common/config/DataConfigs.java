package me.melontini.andromeda.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonDataLoader;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.lang.reflect.Field;
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
        return CompletableFuture.runAsync(() -> {
            Map<Identifier, Map<Module<?>, Set<Tuple<Set<Field>, ? extends BasicConfig>>>> configs = new HashMap<>();
            data.forEach((id, object) -> {
                var m = ModuleManager.get().getModule(id.getPath()).orElseThrow(() -> new IllegalStateException("Invalid module path '%s'!".formatted(id.getPath())));
                var cls = ModuleManager.get().getConfigClass(m.getClass());
                object.entrySet().forEach(entry -> {
                    var map = configs.computeIfAbsent(Identifier.tryParse(entry.getKey()), string -> new HashMap<>());

                    var instance = this.gson.fromJson(entry.getValue(), cls);
                    Set<Field> config = new HashSet<>();
                    entry.getValue().getAsJsonObject().entrySet().forEach(entry2 -> {
                        try {
                            config.add(cls.getField(entry2.getKey()));
                        } catch (NoSuchFieldException e) {
                            throw new CompletionException("Failed to load config data for module '%s'".formatted(m.meta().id()), e);
                        }
                    });
                    map.computeIfAbsent(m, module -> new LinkedHashSet<>()).add(Tuple.of(config, instance));
                });
            });
            CONFIGS = Collections.unmodifiableMap(configs);
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return Common.id("data_configs");
    }
}
