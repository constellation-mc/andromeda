package me.melontini.andromeda.common.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.BootstrapConfig;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.IdentifiedJsonDataLoader;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class DataConfigs extends IdentifiedJsonDataLoader {

    public static final Identifier DEFAULT = new Identifier(MODID, "default");
    public static final ReloaderType<DataConfigs> RELOADER = ReloaderType.create(Andromeda.id("scoped_config"));

    public DataConfigs() {
        super(RELOADER.identifier());
    }

    public static DataConfigs get(MinecraftServer server) {
        try {
            return server.dm$getReloader(RELOADER);
        } catch (Exception e) {
            throw AndromedaException.builder().cause(e).report(false)
                    .translatable("scoped_configs.no_reloader")
                    .build();
        }
    }

    public Map<Identifier, Map<Module<?>, Set<Data>>> configs;
    public Map<Module<?>, Set<Data>> defaultConfigs;

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        if (!Experiments.get().scopedConfigs) return;

        Map<Identifier, Map<Module<?>, Set<CompletableFuture<Data>>>> configs = new Object2ObjectOpenHashMap<>();
        Maps.transformValues(data, JsonElement::getAsJsonObject).forEach((id, object) -> {
            var m = ModuleManager.get().getModule(id.getPath()).orElseThrow(() -> new IllegalStateException("Invalid module path '%s'! The module must be enabled!".formatted(id.getPath())));
            var cls = ModuleManager.getConfigClass(m.getClass());

            if (Andromeda.getConfig(m).e.scope.isWorld()) {
                if (!object.has(DEFAULT.toString()) || object.size() > 1)
                    throw new IllegalStateException("'%s' modules only support '%s' as their dimension!".formatted(BootstrapConfig.Scope.WORLD, DEFAULT));

                var map = configs.computeIfAbsent(DEFAULT, identifier -> new Reference2ObjectOpenHashMap<>());
                map.computeIfAbsent(m, module -> new ReferenceLinkedOpenHashSet<>())
                        .add(makeFuture(m, cls, object.get(DEFAULT.toString())));
                return;
            } else if (Andromeda.getConfig(m).e.scope.isDimension()) {
                object.entrySet().forEach(entry -> {
                    var map = configs.computeIfAbsent(Identifier.tryParse(entry.getKey()), string -> new Reference2ObjectOpenHashMap<>());
                    map.computeIfAbsent(m, module -> new ReferenceLinkedOpenHashSet<>())
                            .add(makeFuture(m, cls, entry.getValue()));
                });
                return;
            }
            throw new IllegalStateException("%s has an invalid scope!".formatted(m.meta().id()));
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

    private CompletableFuture<Data> makeFuture(Module<?> m, Class<? extends Module.BaseConfig> cls, JsonElement element) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var parsed = Andromeda.rootHandler().parse(element, m);
                var specified = new HashSet<>(element.getAsJsonObject().keySet());

                ReferenceOpenHashSet<Field> bootstrap = new ReferenceOpenHashSet<>();
                ReferenceOpenHashSet<Field> config = new ReferenceOpenHashSet<>();

                for (String field : specified) {
                    try {
                        bootstrap.add(assertScoped(BootstrapConfig.class.getField(field)));
                    } catch (NoSuchFieldException e) {
                        try {
                            config.add(assertScoped(cls.getField(field)));
                        } catch (NoSuchFieldException e1) {
                            throw new RuntimeException("Failed to load config data for module '%s'".formatted(m.meta().id()), e1);
                        }
                    }
                }
                return new Data(config, bootstrap, parsed);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load config data for module '%s'".formatted(m.meta().id()), e);
            }
        }, Util.getMainWorkerExecutor());
    }

    private static Field assertScoped(Field field) {
        if (field.isAnnotationPresent(Unscoped.class))
            throw new IllegalStateException("Attempted to modify an unscoped field '%s'!".formatted(field.getName()));
        return field;
    }

    public record Data(Set<Field> cFields, Set<Field> eFields, ConfigHandler.Entry<?, BootstrapConfig> config) {
    }

    public void apply(ScopedConfigs.AttachmentGetter getter, Identifier identifier) {
        if (!Experiments.get().scopedConfigs) return;
        MakeSure.notNull(configs);

        ConfigHandler<BootstrapConfig> attachment = getter.andromeda$getConfigs();
        attachment.loadAll();
        attachment.forEach((entry, module) -> applyDataPacks(entry, module, identifier));
        attachment.saveAll();
    }

    private void apply(ConfigHandler.Entry<?, BootstrapConfig> config, Data data) {
        data.cFields().forEach((field) -> {
            try {
                field.set(config.c, field.get(data.config().c));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to apply config data for module '%s'".formatted(config.c.getClass().getSimpleName()), e);
            }
        });

        data.eFields().forEach((field) -> {
            try {
                field.set(config.e, field.get(data.config().e));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to apply config data for module '%s'".formatted(config.e.getClass().getSimpleName()), e);
            }
        });
    }

    void applyDataPacks(ConfigHandler.Entry<?, BootstrapConfig> config, Module<?> m, Identifier id) {
        if (defaultConfigs != null) {
            var forModule = defaultConfigs.get(m);
            if (forModule != null) {
                for (Data data : forModule) apply(config, data);
            }
        }
        if (id.equals(DEFAULT)) return;

        var data = Objects.requireNonNull(configs).get(id);
        if (data != null) {
            var forModule = data.get(m);
            if (forModule != null) {
                for (Data data1 : forModule) apply(config, data1);
            }
        }
    }
}
