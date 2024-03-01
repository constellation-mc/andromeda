package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.andromeda.base.events.Bus;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The ModuleManager is responsible for resolving and storing modules. It is also responsible for loading and fixing configs.
 */
@CustomLog
public class ModuleManager {

    public static final List<String> CATEGORIES = List.of("world", "blocks", "entities", "items", "bugfixes", "mechanics", "gui", "misc");

    private static ModuleManager INSTANCE;

    private final Map<Class<?>, PromiseImpl<?>> discoveredModules;
    private final Map<String, PromiseImpl<?>> discoveredModuleNames;

    private final Map<Class<?>, Module<?>> modules;
    private final Map<String, Module<?>> moduleNames;

    final Map<String, Module<?>> mixinConfigs = new HashMap<>();

    ModuleManager(List<Module.Zygote> zygotes) {
        if (INSTANCE != null) throw new IllegalStateException("ModuleManager already initialized!");
        INSTANCE = this;

        this.discoveredModules = Utilities.supply(() -> {
            var m = zygotes.stream().collect(Collectors.toMap(Module.Zygote::type, PromiseImpl::new, (t, t2) -> t, LinkedHashMap::new));
            return Collections.unmodifiableMap(m);
        });
        this.discoveredModuleNames = Utilities.supply(() -> {
            var m = zygotes.stream().collect(Collectors.toMap(module -> module.meta().id(), o -> this.discoveredModules.get(o.type()), (t, t2) -> t, HashMap::new));
            return Collections.unmodifiableMap(m);
        });

        List<? extends Module<?>> sorted = zygotes.stream().map(Module.Zygote::supplier).map(s -> {
            discoveredModules.get(s.get().getClass()).future().complete(Utilities.cast(s.get()));
            return s.get();
        }).toList();

        this.setUpConfigs(sorted);

        sorted.forEach(module -> {
            module.config = Utilities.cast(module.manager.load(FabricLoader.getInstance().getConfigDir()));
            module.defaultConfig = Utilities.cast(module.manager.createDefault());
        });

        if (Debug.Keys.ENABLE_ALL_MODULES.isPresent())
            sorted.forEach(module -> module.config().enabled = true);
        fixScopes(sorted);

        sorted.forEach(Module::save);

        this.modules = Utilities.supply(() -> {
            var m = sorted.stream().filter(Module::enabled).collect(Collectors.toMap(Object::getClass, Function.identity(), (t, t2) -> t, LinkedHashMap::new));
            return Collections.unmodifiableMap(m);
        });
        this.moduleNames = Utilities.supply(() -> {
            var m = sorted.stream().filter(Module::enabled).collect(Collectors.toMap(module -> module.meta().id(), Function.identity(), (t, t2) -> t, HashMap::new));
            return Collections.unmodifiableMap(m);
        });

        cleanConfigs(FabricLoader.getInstance().getConfigDir().resolve("andromeda"), sorted);
    }

    private void fixScopes(Collection<? extends Module<?>> modules) {
        modules.forEach(m -> {
            if (Debug.Keys.FORCE_DIMENSION_SCOPE.isPresent()) m.config().scope = Module.BaseConfig.Scope.DIMENSION;

            if (m.meta().environment().isClient() && m.config().scope != Module.BaseConfig.Scope.GLOBAL) {
                if (!Debug.Keys.FORCE_DIMENSION_SCOPE.isPresent())
                    LOGGER.error("{} Module '{}' has an invalid scope ({}), must be {}",
                            m.meta().environment(), m.meta().id(), m.config().scope, Module.BaseConfig.Scope.GLOBAL);
                m.config().scope = Module.BaseConfig.Scope.GLOBAL;
                return;
            }

            if (m.getClass().isAnnotationPresent(Unscoped.class) && m.config().scope != Module.BaseConfig.Scope.GLOBAL) {
                if (!Debug.Keys.FORCE_DIMENSION_SCOPE.isPresent())
                    LOGGER.error("{} Module '{}' has an invalid scope ({}), must be {}",
                            "Unscoped", m.meta().id(), m.config().scope, Module.BaseConfig.Scope.GLOBAL);
                m.config().scope = Module.BaseConfig.Scope.GLOBAL;
            }
        });
    }

    static void validateZygote(Module.Zygote module) {
        MakeSure.notEmpty(module.meta().category(), "Module category can't be null or empty! Module: " + module.getClass());
        MakeSure.isTrue(!module.meta().category().contains("/"), "Module category can't contain '/'! Module: " + module.getClass());
        MakeSure.notEmpty(module.meta().name(), "Module name can't be null or empty! Module: " + module.getClass());
    }

    private void setUpConfigs(Collection<? extends Module<?>> modules) {
        modules.forEach(m -> {
            var manager = makeManager(m);
            manager.onLoad((config1, path) -> {
                if (AndromedaConfig.get().sideOnlyMode) {
                    switch (m.meta().environment()) {
                        case BOTH -> config1.enabled = false;
                        case CLIENT -> {
                            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
                                config1.enabled = false;
                        }
                        case SERVER -> {
                            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER)
                                config1.enabled = false;
                        }
                    }
                }
            });
            manager.exceptionHandler((e, stage, path) -> LOGGER.error("Failed to %s config for module: %s".formatted(stage.toString().toLowerCase(), m.meta().id()), e));

            Bus<ConfigEvent<?>> e = m.getOrCreateBus(ConfigEvent.class, null);
            if (e != null) e.invoker().accept(Utilities.cast(manager));

            m.manager = Utilities.cast(manager);
        });
    }

    private ConfigManager<? extends Module.BaseConfig> makeManager(Module<?> m) {
        Class<? extends Module.BaseConfig> cls = getConfigClass(m.getClass());

        return cls == Module.BaseConfig.class ?
                ConfigManager.of(Module.BaseConfig.class, "andromeda/" + m.meta().id(), Module.BaseConfig::new) :
                ConfigManager.of(cls, "andromeda/" + m.meta().id());
    }

    /**
     * Parses the config class from modules generic type.
     *
     * @param m the module class.
     * @return the config class.
     */
    public Class<? extends Module.BaseConfig> getConfigClass(Class<?> m) {
        if (m.getGenericSuperclass() instanceof ParameterizedType pt) {
            for (Type ta : pt.getActualTypeArguments()) {
                if (ta instanceof Class<?> cls && Module.BaseConfig.class.isAssignableFrom(cls)) {
                    return Utilities.cast(cls);
                }
            }
        }
        return !Object.class.equals(m.getSuperclass()) ? getConfigClass(m.getSuperclass()) : Module.BaseConfig.class;
    }

    public void cleanConfigs(Path root, Collection<? extends Module<?>> modules) {
        if (Files.exists(root)) {
            Set<Path> paths = collectPaths(root.getParent(), modules);
            Bootstrap.wrapIO(() -> Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".json") && !Files.isHidden(file) && !paths.contains(file)) {
                        Files.delete(file);
                        LOGGER.info("Removed {} as it doesn't belong to any module!", FabricLoader.getInstance().getGameDir().relativize(file));
                    }
                    return super.visitFile(file, attrs);
                }
            }), "Failed to clean up configs!");
        }
    }

    private Set<Path> collectPaths(Path root, Collection<? extends Module<?>> modules) {
        Set<Path> paths = new HashSet<>();

        paths.add(root.resolve("andromeda/mod.json"));
        paths.add(root.resolve("andromeda/debug.json"));

        modules.forEach(module -> paths.add(module.manager().resolve(root)));

        return paths;
    }

    /**
     * Checks if a module is present.
     *
     * @param cls the module class.
     * @param <T> the module type.
     * @return if a module is enabled.
     */
    public <T extends Module<?>> boolean isPresent(Class<T> cls) {
        return getModule(cls).isPresent();
    }

    /**
     * Returns the module of the given class, but only if it is enabled.
     *
     * @param cls the module class.
     * @param <T> the module type.
     * @return The module, if enabled, or empty if not.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getModule(Class<T> cls) {
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
    public <T extends Module<?>> Optional<T> getModule(String name) {
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
    public <T extends Module<?>> Optional<Promise<T>> getDiscovered(Class<T> cls) {
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
    public <T extends Module<?>> Optional<Promise<T>> getDiscovered(String name) {
        return Optional.ofNullable((Promise<T>) discoveredModuleNames.get(name));
    }

    @ApiStatus.Internal
    public Optional<Module<?>> moduleFromConfig(String name) {
        return Optional.ofNullable(mixinConfigs.get(name));
    }

    @ApiStatus.Internal
    public Collection<Promise<?>> all() {
        return Collections.unmodifiableCollection(discoveredModules.values());//Java generics moment
    }

    /**
     * @return a collection of all loaded modules.
     */
    public Collection<Module<?>> loaded() {
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
    public static <T extends Module<?>> T quick(Class<T> cls) {
        return get().getModule(cls).orElseThrow(() -> new IllegalStateException("Module %s requested quickly, but is not loaded.".formatted(cls)));
    }

    /**
     * @return The module manager.
     */
    public static ModuleManager get() {
        return MakeSure.notNull(INSTANCE, "ModuleManager requested too early!");
    }

    void print() {
        Map<String, Set<Module<?>>> categories = Utilities.consume(new LinkedHashMap<>(), map -> get().loaded().forEach(m ->
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
            LOGGER.info("Loaded modules: {}", builder);
            LOGGER.info("* - custom modules/categories not provided by Andromeda.");
        } else {
            LOGGER.info("No modules loaded!");
        }
    }

    public interface ModuleSupplier {
        List<Module.Zygote> get();
    }
}
