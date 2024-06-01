package me.melontini.andromeda.base;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.CustomLog;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.base.events.Bus;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.util.BootstrapConfig;
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.EarlyLanguage;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The ModuleManager is responsible for resolving and storing modules. It is also responsible for loading and fixing configs.
 */
@CustomLog @Accessors(fluent = true)
public final class ModuleManager {

    public static final List<String> CATEGORIES = List.of("world", "blocks", "entities", "items", "bugfixes", "mechanics", "gui", "misc");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Nullable static ModuleManager INSTANCE;

    private final Map<Class<?>, PromiseImpl<?>> discoveredModules;
    private final Map<String, PromiseImpl<?>> discoveredModuleNames;

    private final Map<Class<?>, Module> modules;
    private final Map<String, Module> moduleNames;

    @Setter
    Function<Module, BootstrapConfig> configGetter;

    private final MixinProcessor mixinProcessor;

    ModuleManager(List<Module.Zygote> zygotes) {
        this.mixinProcessor = new MixinProcessor(this);

        this.discoveredModules = Utilities.supply(() -> {
            var m = zygotes.stream().collect(Collectors.toMap(Module.Zygote::type, PromiseImpl::new, (t, t2) -> t, LinkedHashMap::new));
            return Collections.unmodifiableMap(m);
        });
        this.discoveredModuleNames = Utilities.supply(() -> {
            var m = zygotes.stream().collect(Collectors.toMap(module -> module.meta().id(), o -> this.discoveredModules.get(o.type()), (t, t2) -> t, HashMap::new));
            return Collections.unmodifiableMap(m);
        });

        List<? extends Module> sorted = zygotes.stream().map(Module.Zygote::supplier).map(s -> {
            discoveredModules.get(s.get().getClass()).future().complete(Utilities.cast(s.get()));
            return s.get();
        }).toList();

        sorted.forEach(module -> ConfigEvent.bootstrap(module).listen((manager, config) -> {
            if (AndromedaConfig.get().sideOnlyMode) {
                var env = module.meta().environment();
                if (CommonValues.environment() == EnvType.CLIENT) {
                    if (env.isServer()) config.enabled = false;
                } else {
                    if (env.isClient()) config.enabled = false;
                }
                if (env.isBoth()) config.enabled = false;
            }
        }));

        LOGGER.info("Loading bootstrap configs!");
        Map<Module, CompletableFuture<BootstrapConfig>> configs = new IdentityHashMap<>();
        sorted.forEach(m -> configs.put(m, CompletableFuture.supplyAsync(() -> {
            var path = FabricLoader.getInstance().getConfigDir().resolve("andromeda/" + m.meta().id() + ".json");
            if (!Files.exists(path)) return new BootstrapConfig();

            try (var reader = Files.newBufferedReader(path)) {
                JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                if (object.has("bootstrap")) object = object.getAsJsonObject("bootstrap");
                return Objects.requireNonNull(GSON.fromJson(object, BootstrapConfig.class));
            } catch (Exception e) {
                LOGGER.error("Failed to load {}! Resetting to default!", FabricLoader.getInstance().getGameDir().relativize(path), e);
                return new BootstrapConfig();
            }
        })));
        Map<Module, BootstrapConfig> bootstrapConfigs = new IdentityHashMap<>(Maps.transformValues(configs, CompletableFuture::join));
        this.configGetter = bootstrapConfigs::get;
        bootstrapConfigs.forEach((module, config) -> {
            Bus<ConfigEvent> bus = module.getOrCreateBus("bootstrap_config_event", null);
            if (bus == null) return;
            bus.invoker().accept(this, config);
        });

        if (Debug.Keys.ENABLE_ALL_MODULES.isPresent()) bootstrapConfigs.values().forEach(c -> c.enabled = true);
        fixScopes(sorted);

        sorted.forEach(this::saveBootstrap);

        this.modules = Utilities.supply(() -> {
            var m = sorted.stream().filter(module -> getConfig(module).enabled()).collect(Collectors.toMap(Object::getClass, Function.identity(), (t, t2) -> t, LinkedHashMap::new));
            return Collections.unmodifiableMap(m);
        });
        this.moduleNames = Utilities.supply(() -> {
            var m = sorted.stream().filter(module -> getConfig(module).enabled()).collect(Collectors.toMap(module -> module.meta().id(), Function.identity(), (t, t2) -> t, HashMap::new));
            return Collections.unmodifiableMap(m);
        });

        cleanConfigs(FabricLoader.getInstance().getConfigDir().resolve("andromeda"), sorted);
    }

    private void fixScopes(Collection<? extends Module> modules) {
        modules.forEach(m -> {
            var config = this.getConfig(m);
            if (Debug.Keys.FORCE_DIMENSION_SCOPE.isPresent()) config.scope = BootstrapConfig.Scope.DIMENSION;

            if (!Experiments.get().scopedConfigs && !config.scope.isGlobal()) {
                throw AndromedaException.builder().report(false)
                        .translatable("module_manager.scoped_configs_disabled", m.meta().id(), config.scope)
                        .build();
            }

            if (m.meta().environment().isClient() && !config.scope.isGlobal()) {
                if (!Debug.Keys.FORCE_DIMENSION_SCOPE.isPresent())
                    LOGGER.error(EarlyLanguage.translate("andromeda.module_manager.invalid_scope", m.meta().environment(), m.meta().id(), config.scope, BootstrapConfig.Scope.GLOBAL));
                config.scope = BootstrapConfig.Scope.GLOBAL;
                return;
            }

            if (m.getClass().isAnnotationPresent(Unscoped.class) && !config.scope.isGlobal()) {
                if (!Debug.Keys.FORCE_DIMENSION_SCOPE.isPresent())
                    LOGGER.error(EarlyLanguage.translate("andromeda.module_manager.invalid_scope", "Unscoped", m.meta().id(), config.scope, BootstrapConfig.Scope.GLOBAL));
                config.scope = BootstrapConfig.Scope.GLOBAL;
            }
        });
    }

    static void validateZygote(@NonNull Module.Zygote module) {
        MakeSure.notEmpty(module.meta().category(), "Module category can't be null or empty! Module: " + module.getClass());
        MakeSure.isTrue(!module.meta().category().contains("/"), "Module category can't contain '/'! Module: " + module.getClass());
        MakeSure.notEmpty(module.meta().name(), "Module name can't be null or empty! Module: " + module.getClass());
    }

    public void cleanConfigs(Path root, Collection<? extends Module> modules) {
        if (Files.exists(root)) {
            Set<Path> paths = collectPaths(Objects.requireNonNull(root.getParent(), () -> "Root config folder? %s".formatted(root)), modules);
            Bootstrap.wrapIO(() -> Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".json") && !Files.isHidden(file) && !paths.contains(file)) {
                        Files.delete(file);
                        LOGGER.info(EarlyLanguage.translate("andromeda.module_manager.removed_unlinked_config", FabricLoader.getInstance().getGameDir().relativize(file)));
                    }
                    return super.visitFile(file, attrs);
                }
            }), "Failed to clean up configs!");
        }
    }

    private Set<Path> collectPaths(@NonNull Path root, @NonNull Collection<? extends Module> modules) {
        Set<Path> paths = new HashSet<>();

        paths.add(root.resolve("andromeda/mod.json"));
        paths.add(root.resolve("andromeda/debug.json"));
        paths.add(root.resolve("andromeda/experiments.json"));

        modules.forEach(module -> paths.add(root.resolve("andromeda/" + module.meta().id() + ".json")));

        return Collections.unmodifiableSet(paths);
    }

    public void saveBootstrap(Module module) {
        var path = FabricLoader.getInstance().getConfigDir().resolve("andromeda/" + module.meta().id() + ".json");

        JsonObject object = new JsonObject();
        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path)) {
                object = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException | RuntimeException e) {
                object = new JsonObject();
            }
        }

        var cfg = getConfig(module);
        var o = GSON.toJsonTree(cfg).getAsJsonObject();
        if (!object.has("bootstrap")) o.asMap().keySet().forEach(object::remove);
        object.add("bootstrap", o);

        try {
            var parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(path, GSON.toJson(object));
        } catch (IOException e) {
            LOGGER.error("Failed to save {}!", FabricLoader.getInstance().getGameDir().relativize(path), e);
        }
    }

    public BootstrapConfig getConfig(Module module) {
        return this.configGetter.apply(module);
    }

    /**
     * Checks if a module is present.
     *
     * @param cls the module class.
     * @param <T> the module type.
     * @return if a module is enabled.
     */
    public <T extends Module> boolean isPresent(Class<T> cls) {
        return modules.containsKey(cls);
    }

    /**
     * Returns the module of the given class, but only if it is enabled.
     *
     * @param cls the module class.
     * @param <T> the module type.
     * @return The module, if enabled, or empty if not.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> Optional<T> getModule(Class<T> cls) {
        return (Optional<T>) Optional.ofNullable(modules.get(cls));
    }

    /**
     * Returns the module of the given id, but only if it is enabled.
     *
     * @param name the module id.
     * @param <T>  the module type.
     * @return The module, if enabled, or empty if not.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> Optional<T> getModule(String name) {
        return (Optional<T>) Optional.ofNullable(moduleNames.get(name));
    }

    /**
     * Returns the module of the given class.
     * <p>This will also return disabled modules. This should only be used during {@link Bootstrap.Status#SETUP}.</p>
     * <p>Due to the way Andromeda is loaded, executors must not be used to avoid deadlocking the game</p>
     *
     * @param cls the module class.
     * @param <T> the module type.
     * @return The module future, if discovered, or empty if not.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> Optional<Promise<T>> getDiscovered(Class<T> cls) {
        return Optional.ofNullable((Promise<T>) discoveredModules.get(cls));
    }

    /**
     * Returns the module of the given id.
     * <p>This will also return disabled modules. This should only be used during {@link Bootstrap.Status#SETUP}.</p>
     * <p>Due to the way Andromeda is loaded, executors must not be used to avoid deadlocking the game</p>
     *
     * @param name the module id.
     * @param <T>  the module type.
     * @return The module future, if discovered, or empty if not.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> Optional<Promise<T>> getDiscovered(String name) {
        return Optional.ofNullable((Promise<T>) discoveredModuleNames.get(name));
    }

    @ApiStatus.Internal
    public MixinProcessor getMixinProcessor() {
        return this.mixinProcessor;
    }

    @ApiStatus.Internal
    public Collection<Promise<?>> all() {
        return Collections.unmodifiableCollection(discoveredModules.values());//Java generics moment
    }

    /**
     * Returns a collection of all loaded modules.
     *
     * @return a collection of all loaded modules.
     */
    public Collection<Module> loaded() {
        return Collections.unmodifiableCollection(modules.values());
    }

    /**
     * Quickly returns a module of the given class. Useful for mixins and registration. Must never be used in non-mixin static fields and class initializers.
     * <p>This will throw an {@link IllegalStateException} if the module is not loaded.</p>
     *
     * @param cls the module class.
     * @param <T> the module time.
     * @return the module instance.
     * @throws IllegalStateException if the module is not loaded.
     */
    public static <T extends Module> T quick(Class<T> cls) {
        return get().getModule(cls).orElseThrow(() -> new IllegalStateException("Module %s requested quickly, but is not loaded.".formatted(cls)));
    }

    /**
     * Returns The module manager.
     *
     * @return The module manager.
     */
    public static ModuleManager get() {
        return Objects.requireNonNull(INSTANCE, "ModuleManager requested too early!");
    }

    void print() {
        Map<String, Set<Module>> categories = Utilities.supply(new LinkedHashMap<>(), map -> loaded().forEach(m ->
                map.computeIfAbsent(m.meta().category(), s -> new LinkedHashSet<>()).add(m)));

        StringBuilder builder = new StringBuilder();
        categories.forEach((s, strings) -> {
            builder.append("\n\t - ").append(s);
            if (!ModuleManager.CATEGORIES.contains(s)) builder.append("*");
            builder.append("\n\t  |-- ");

            StringJoiner joiner = new StringJoiner(", ");
            strings.forEach(m -> joiner.add(m.meta().name().replace('/', '.') +
                    (!m.getClass().getName().startsWith("me.melontini.andromeda") ? '*' : "")));
            builder.append(joiner);
        });
        if (!categories.isEmpty()) {
            LOGGER.info(EarlyLanguage.translate("andromeda.module_manager.loading_modules", loaded().size()) +" {}", builder);
            LOGGER.info("* - %s".formatted(EarlyLanguage.translate("andromeda.module_manager.custom_modules")));
        } else {
            LOGGER.info(EarlyLanguage.translate("andromeda.module_manager.no_modules"));
        }
    }

    public interface ModuleSupplier {
        List<Module.Zygote> get();
    }
}
