package me.melontini.andromeda.base.util;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.Setter;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.dark_matter.api.base.util.Exceptions;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ConfigHandler<E> {

    private final Map<Module<?>, Entry<?, E>> configs = new IdentityHashMap<>();
    private final Map<Module<?>, Entry<?, E>> defaultConfigs = new IdentityHashMap<>();

    private final Path path;
    private final Class<E> extension;
    private final Collection<? extends Module<?>> modules;
    private final Gson gson;

    @Setter
    private ConfigHandler<E> root;

    public ConfigHandler(Path path, Collection<? extends Module<?>> modules, Class<E> extension) {
        this.path = path;
        this.modules = modules;
        this.extension = extension;
        var builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Identifier.class, new GsonContext<>(Identifier.CODEC));
        this.gson = builder.create();
    }

    public Path resolve(Module<?> module) {
        return this.path.resolve("andromeda/" + module.meta().id() + ".json");
    }

    public <T extends Module.BaseConfig> Entry<T, E> get(Class<? extends Module<T>> cls) {
        return get(ModuleManager.quick(cls));
    }

    public <T extends Module.BaseConfig> Entry<T, E> get(Module<T> module) {
        return (Entry<T, E>) this.configs.get(module);
    }

    public <T extends Module.BaseConfig> Entry<T, E> getDefault(Class<? extends Module<T>> cls) {
        return get(ModuleManager.quick(cls));
    }

    public <T extends Module.BaseConfig> Entry<T, E> getDefault(Module<T> module) {
        var entry = (Entry<T, E>) this.defaultConfigs.get(module);
        if (entry == null) {
            if (root != null) return root.getDefault(module);

            synchronized (this.defaultConfigs) {
                entry = Exceptions.supply(() -> {
                    var ext = this.extension.getConstructor().newInstance();
                    var c = ModuleManager.getConfigClass(module.getClass()).getConstructor().newInstance();

                    return new Entry<>((T) c, ext);
                });
                this.defaultConfigs.put(module, entry);
            }
        }
        return entry;
    }

    public void forEach(BiConsumer<ConfigHandler.Entry<?, E>, Module<?>> consumer) {
        this.configs.forEach((module, eEntry) -> consumer.accept(eEntry, module));
    }

    public void save(Module<?> module) throws IOException {
        if (!this.modules.contains(module)) throw new IllegalStateException(module.meta().id());
        var path = resolve(module);

        var ext = this.gson.toJsonTree(get(module).e).getAsJsonObject();
        this.gson.toJsonTree(get(module).c).getAsJsonObject().asMap().forEach(ext::add);

        if (path.getParent() != null) Files.createDirectories(path.getParent());
        Files.writeString(path, this.gson.toJson(ext));
    }

    public <T extends Module.BaseConfig> Entry<T, E> parse(JsonElement element, Module<T> module) {
        if (!element.isJsonObject()) throw new IllegalStateException("Not a JsonObject!");

        JsonObject object = element.getAsJsonObject();
        var ext = this.gson.fromJson(object, this.extension);
        var c = this.gson.fromJson(object, ModuleManager.getConfigClass(module.getClass()));
        return new Entry<>((T) c, ext);
    }

    private <T extends Module.BaseConfig> Entry<T, E> load(Module<T> module) throws IOException {
        if (!this.modules.contains(module)) throw new IllegalStateException(module.meta().id());
        var path = resolve(module);
        if (!Files.exists(path)) {
            if (root != null) return root.load(module);

            return Exceptions.supply(() -> {
                var ext = this.extension.getConstructor().newInstance();
                var c = ModuleManager.getConfigClass(module.getClass()).getConstructor().newInstance();
                return new Entry<>((T) c, ext);
            });
        }

        try (var reader = Files.newBufferedReader(path)) {
            return parse(JsonParser.parseReader(reader), module);
        }
    }

    public void saveAll() {
        CompletableFuture.allOf(this.modules.stream().map(module ->
                        CompletableFuture.runAsync(() -> Exceptions.run(() -> this.save(module))))
                .toArray(CompletableFuture[]::new)).join();
    }

    public void loadAll() {
        Map<Module<?>, CompletableFuture<Entry<?, E>>> configs = new IdentityHashMap<>();
        for (Module<?> module : this.modules) {
            configs.put(module, CompletableFuture.supplyAsync(() -> Exceptions.supply(() -> this.load(module))));
        }
        this.configs.putAll(Maps.transformValues(configs, CompletableFuture::join));
    }

    public static final class Entry<C extends Module.BaseConfig, E> {
        public final C c;
        public final E e;

        public Entry(C config, E ext) {
            this.c = config;
            this.e = ext;
        }
    }

    public record GsonContext<C>(Codec<C> codec) implements JsonSerializer<C>, JsonDeserializer<C> {

        @Override
        public C deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return this.codec.parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {
                throw new JsonParseException(s);
            });
        }

        @Override
        public JsonElement serialize(C src, Type typeOfSrc, JsonSerializationContext context) {
            return codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow(false, s -> {
                throw new IllegalStateException(s);
            });
        }
    }
}
