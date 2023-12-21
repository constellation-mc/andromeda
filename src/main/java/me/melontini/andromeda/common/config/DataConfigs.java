package me.melontini.andromeda.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonDataLoader;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DataConfigs extends JsonDataLoader {
    public DataConfigs() {
        super(new Gson(), "andromeda/scoped_config");
    }

    public static Map<Identifier, Map<Module<?>, ? extends BasicConfig>> CONFIGS;

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            Map<Identifier, Map<Module<?>, ? extends BasicConfig>> configs = new HashMap<>();
            data.forEach((id, object) -> {
                var m = ModuleManager.get().getModule(id.getPath()).orElseThrow();
                object.entrySet().forEach(entry ->
                        configs.computeIfAbsent(Identifier.tryParse(entry.getKey()), string -> new HashMap<>())
                                .put(m, Utilities.cast(this.gson.fromJson(entry.getValue(), ModuleManager.get().getConfigClass(m.getClass())))));
            });
            CONFIGS = Collections.unmodifiableMap(configs);
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return Common.id("data_configs");
    }
}
