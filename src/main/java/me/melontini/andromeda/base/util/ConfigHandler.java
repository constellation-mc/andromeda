package me.melontini.andromeda.base.util;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.CustomLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.ConfigGsonEvent;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@CustomLog
public class ConfigHandler {

    private final Map<Module<?>, Entry<?>> configs = new IdentityHashMap<>();
    private final Map<Module<?>, Entry<?>> defaultConfigs = new IdentityHashMap<>();

    private final Path path;
    private final Collection<? extends Module<?>> modules;
    @Getter
    private final Gson gson;

    @Setter
    private ConfigHandler root;

    public ConfigHandler(Path path, Collection<? extends Module<?>> modules) {
        this.path = path;
        this.modules = modules;
        var builder = new GsonBuilder().setPrettyPrinting();
        ConfigGsonEvent.BUS.invoker().accept(builder);
        this.gson = builder.create();
    }

    public Path resolve(Module<?> module) {
        return this.path.resolve("andromeda/" + module.meta().id() + ".json");
    }

    public <T extends Module.BaseConfig> Entry<T> get(Class<? extends Module<T>> cls) {
        return get(ModuleManager.quick(cls));
    }

    public <T extends Module.BaseConfig> Entry<T> get(Module<T> module) {
        return (Entry<T>) this.configs.get(module);
    }

    public <T extends Module.BaseConfig> Entry<T> getDefault(Class<? extends Module<T>> cls) {
        return get(ModuleManager.quick(cls));
    }

    public <T extends Module.BaseConfig> Entry<T> getDefault(Module<T> module) {
        var entry = (Entry<T>) this.defaultConfigs.get(module);
        if (entry == null) {
            if (root != null) return root.getDefault(module);

            synchronized (this.defaultConfigs) {
                entry = Exceptions.supply(() -> {
                    var ext = BootstrapConfig.class.getConstructor().newInstance();
                    var c = ModuleManager.getConfigClass(module.getClass()).getConstructor().newInstance();

                    return new Entry<>((T) c, ext);
                });
                this.defaultConfigs.put(module, entry);
            }
        }
        return entry;
    }

    public void forEach(BiConsumer<ConfigHandler.Entry<?>, Module<?>> consumer) {
        this.configs.forEach((module, eEntry) -> consumer.accept(eEntry, module));
    }

    public void save(Module<?> module) {
        if (!this.modules.contains(module)) throw new IllegalStateException(module.meta().id());
        var path = resolve(module);

        var entry = get(module);
        try {
            var ext = this.gson.toJsonTree(entry.e).getAsJsonObject();
            this.gson.toJsonTree(entry.c).getAsJsonObject().asMap().forEach(ext::add);

            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, this.gson.toJson(ext));
        } catch (Exception e) {
            LOGGER.error("Failed to save {}!", FabricLoader.getInstance().getGameDir().relativize(path), e);
        }
    }

    public <T extends Module.BaseConfig> Entry<T> parse(JsonElement element, Module<T> module) {
        if (!element.isJsonObject()) throw new IllegalStateException("Not a JsonObject!");

        JsonObject object = element.getAsJsonObject();
        var ext = Objects.requireNonNull(this.gson.fromJson(object, BootstrapConfig.class));
        var c = Objects.requireNonNull(this.gson.fromJson(object, ModuleManager.getConfigClass(module.getClass())));
        return new Entry<>((T) c, ext);
    }

    private <T extends Module.BaseConfig> Entry<T> load(Module<T> module) throws IOException {
        if (!this.modules.contains(module)) throw new IllegalStateException(module.meta().id());
        var path = resolve(module);
        if (!Files.exists(path)) {
            if (root != null) return root.load(module);

            return Exceptions.supply(() -> {
                var ext = BootstrapConfig.class.getConstructor().newInstance();
                var c = ModuleManager.getConfigClass(module.getClass()).getConstructor().newInstance();
                return new Entry<>((T) c, ext);
            });
        }

        try (var reader = Files.newBufferedReader(path)) {
            return parse(MakeSure.isTrue(JsonParser.parseReader(reader), JsonElement::isJsonObject), module);
        } catch (Exception e) {
            LOGGER.error("Failed to load {}! Returning default!", FabricLoader.getInstance().getGameDir().relativize(path), e);
            return Exceptions.supply(() -> {
                var ext = BootstrapConfig.class.getConstructor().newInstance();
                var c = ModuleManager.getConfigClass(module.getClass()).getConstructor().newInstance();
                return new Entry<>((T) c, ext);
            });
        }
    }

    public void saveAll() {
        CompletableFuture.allOf(this.modules.stream().map(module ->
                        CompletableFuture.runAsync(() -> this.save(module)))
                .toArray(CompletableFuture[]::new)).join();
    }

    public void loadAll() {
        Map<Module<?>, CompletableFuture<Entry<?>>> configs = new IdentityHashMap<>();
        for (Module<?> module : this.modules) {
            configs.put(module, CompletableFuture.supplyAsync(() -> Exceptions.supply(() -> this.load(module))));
        }
        this.configs.putAll(Maps.transformValues(configs, CompletableFuture::join));
    }

    @RequiredArgsConstructor
    public static final class Entry<C extends Module.BaseConfig> {
        public final C c;
        public final BootstrapConfig e;
    }

    public static <C> GsonContext<C> context(Codec<C> codec) {
        return new GsonContext<>(codec);
    }

    public record GsonContext<C>(Codec<C> codec) implements JsonSerializer<C>, JsonDeserializer<C> {

        @Override
        public C deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var r = this.codec.parse(JsonOps.INSTANCE, json);
            if (r.error().isPresent()) throw new JsonParseException(r.error().orElseThrow().message());
            return r.result().orElseThrow();
        }

        @Override
        public JsonElement serialize(C src, Type typeOfSrc, JsonSerializationContext context) {
            var r = codec.encodeStart(JsonOps.INSTANCE, src);
            if (r.error().isPresent()) throw new IllegalStateException(r.error().orElseThrow().message());
            return r.result().orElseThrow();
        }
    }
}
