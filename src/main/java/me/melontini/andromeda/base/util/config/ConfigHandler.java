package me.melontini.andromeda.base.util.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.CustomLog;
import lombok.Getter;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.ConfigGsonEvent;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@CustomLog
public final class ConfigHandler {

    private final Map<ConfigDefinition<?>, Module.BaseConfig> configs = new IdentityHashMap<>();
    private final Map<ConfigDefinition<?>, Module.BaseConfig> defaultConfigs = new IdentityHashMap<>();

    private final Path path;
    private final boolean topLevel;
    private final ConfigState state;
    private final ConfigHandler root;
    private final Collection<? extends Module> modules;
    @Getter
    private final Gson gson;

    public ConfigHandler(Path path, boolean topLevel, ConfigState state, ConfigHandler root, Collection<? extends Module> modules) {
        this.path = path;
        this.topLevel = topLevel;
        this.state = state;
        this.root = root;
        this.modules = modules.stream().filter(module -> module.getConfigDefinition(state) != null).toList();
        var builder = new GsonBuilder().setPrettyPrinting();
        ConfigGsonEvent.BUS.invoker().accept(builder);
        this.gson = builder.create();
    }

    public ConfigHandler(Path path, ConfigState state, Collection<? extends Module> modules) {
        this(path, false, state, null, modules);
    }

    public Path resolve(Module module) {
        return this.path.resolve("andromeda/" + module.meta().id() + ".json");
    }

    public <T extends Module.BaseConfig> T get(ConfigDefinition<T> module) {
        return (T) this.configs.get(module);
    }

    public <T extends Module.BaseConfig> T getDefault(ConfigDefinition<T> module) {
        var entry = (T) this.defaultConfigs.get(module);
        if (entry == null) {
            if (root != null) return root.getDefault(module);

            synchronized (this.defaultConfigs) {
                entry = Exceptions.supply(() -> module.supplier().get().getConstructor().newInstance());
                this.defaultConfigs.put(module, entry);
            }
        }
        return entry;
    }

    public void forEach(BiConsumer<Module.BaseConfig, Module> consumer) {
        this.modules.forEach(module -> consumer.accept(get(module.getConfigDefinition(state)), module));
    }

    public void save(Module module) {
        if (!this.modules.contains(module)) throw new IllegalStateException(module.meta().id());
        var path = resolve(module);

        var entry = get(module.getConfigDefinition(state));
        try {
            JsonObject object;
            if (!topLevel) {
                if (Files.exists(path)) {
                    try (var reader = Files.newBufferedReader(path)) {
                        object = JsonParser.parseReader(reader).getAsJsonObject();
                    } catch (IOException | JsonParseException e) {
                        object = new JsonObject();
                    }
                } else {
                    object = new JsonObject();
                }

                var o = this.gson.toJsonTree(entry).getAsJsonObject();
                if (!o.asMap().isEmpty()) {
                    object.add(state.name().toLowerCase(Locale.ROOT), o);
                    o.keySet().forEach(object::remove);
                }
            } else {
                object = this.gson.toJsonTree(entry).getAsJsonObject();
            }

            var parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(path, this.gson.toJson(object));
        } catch (Exception e) {
            LOGGER.error("Failed to save {}!", FabricLoader.getInstance().getGameDir().relativize(path), e);
        }
    }

    public void saveAll() {
        CompletableFuture.allOf(this.modules.stream().map(module ->
                        CompletableFuture.runAsync(() -> this.save(module)))
                .toArray(CompletableFuture[]::new)).join();
    }

    public Module.BaseConfig parse(JsonElement element, Module module) {
        if (!element.isJsonObject()) throw new IllegalStateException("Not a JsonObject!");

        JsonObject object = element.getAsJsonObject();
        if (!topLevel) {
            object = Objects.requireNonNullElse(object.getAsJsonObject(state.name().toLowerCase(Locale.ROOT)), object);
        }
        return MakeSure.notNull(this.gson.fromJson(object, module.getConfigDefinition(state).supplier().get()));
    }

    private Module.BaseConfig load(Module module) {
        if (!this.modules.contains(module)) throw new IllegalStateException(module.meta().id());
        var path = resolve(module);
        if (!Files.exists(path)) {
            if (root != null) return root.load(module);

            return Exceptions.supply(() -> module.getConfigDefinition(state).supplier().get().getConstructor().newInstance());
        }

        try (var reader = Files.newBufferedReader(path)) {
            return parse(MakeSure.isTrue(JsonParser.parseReader(reader), JsonElement::isJsonObject), module);
        } catch (Exception e) {
            LOGGER.error("Failed to load {}! Returning default!", FabricLoader.getInstance().getGameDir().relativize(path), e);
            return Exceptions.supply(() -> module.getConfigDefinition(state).supplier().get().getConstructor().newInstance());
        }
    }

    public void loadAll() {
        Map<ConfigDefinition<?>, CompletableFuture<Module.BaseConfig>> configs = new IdentityHashMap<>();
        for (Module module : this.modules) {
            configs.put(module.getConfigDefinition(state), CompletableFuture.supplyAsync(() -> this.load(module)));
        }
        this.configs.putAll(Maps.transformValues(configs, CompletableFuture::join));
    }
}
