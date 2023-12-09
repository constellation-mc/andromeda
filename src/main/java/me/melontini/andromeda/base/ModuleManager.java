package me.melontini.andromeda.base;

import com.google.common.collect.ImmutableMap;
import lombok.CustomLog;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.classes.Lazy;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class ModuleManager {

    private static final List<String> categories = List.of("world", "blocks", "entities", "items", "bugfixes", "mechanics", "gui", "misc");

    private final ImmutableMap<Class<?>, Module<?>> discoveredModules;
    private final ImmutableMap<String, Module<?>> discoveredModuleNames;

    private final ImmutableMap<Class<?>, Module<?>> modules;
    private final ImmutableMap<String, Module<?>> moduleNames;

    private final ImmutableMap<Class<?>, Lazy<ConfigManager<? extends BasicConfig>>> configs;

    final Map<String, Module<?>> mixinConfigs = new HashMap<>();

    ModuleManager(List<Module<?>> discovered) {
        Bootstrap.INSTANCE = this;

        Set<String> ids = new HashSet<>();
        for (Module<?> module : discovered) {
            MakeSure.notEmpty(module.meta().category(), "Module category can't be null or empty! Module: " + module.getClass());
            MakeSure.isTrue(!module.meta().category().contains("/"), "Module category can't contain '/'! Module: " + module.getClass());
            MakeSure.notEmpty(module.meta().name(), "Module name can't be null or empty! Module: " + module.getClass());

            if (ids.contains(module.meta().id()))
                throw new IllegalStateException("Duplicate module IDs! ID: %s, Module: %s".formatted(module.meta().id(), module.getClass()));
            ids.add(module.meta().id());
        }

        List<Module<?>> sorted = discovered.stream().sorted(Comparator.comparingInt(m -> {
            int i = categories.indexOf(m.meta().category());
            return i >= 0 ? i : categories.size();
        })).toList();

        this.discoveredModules = ImmutableMap.copyOf(Utilities.consume(new LinkedHashMap<>(), (map) ->
                sorted.forEach(m -> map.put(m.getClass(), m))));
        this.discoveredModuleNames = ImmutableMap.copyOf(Utilities.consume(new HashMap<>(), (map) ->
                sorted.forEach(m -> map.put(m.meta().id(), m))));

        this.configs = ImmutableMap.copyOf(setUpConfigs(sorted));

        sorted.forEach(Module::postConfig);
        sorted.forEach(module -> module.manager().save());

        this.modules = ImmutableMap.copyOf(Utilities.consume(new LinkedHashMap<>(), map ->
                sorted.forEach(module -> {if (module.enabled()) map.put(module.getClass(), module);})));
        this.moduleNames = ImmutableMap.copyOf(Utilities.consume(new HashMap<>(), map ->
                sorted.forEach(module -> {if (module.enabled()) map.put(module.meta().id(), module);})));

        cleanConfigs();
    }

    public Map<Class<?>, Lazy<ConfigManager<? extends BasicConfig>>> setUpConfigs(List<Module<?>> list) {
        Map<Class<?>, Lazy<ConfigManager<? extends BasicConfig>>> configs = new HashMap<>();

        list.forEach(m -> {
            var config = ConfigBuilder.create(m.configClass(), CommonValues.mod(), "andromeda/" + m.meta().id());
            config.processors((registry, mod) -> {
                registry.register(CommonValues.MODID + ":side_only_enabled", manager -> {
                    if (Config.get().sideOnlyMode) {
                        return switch (m.meta().environment()) {
                            case ANY -> null;
                            case CLIENT -> FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ?
                                    null : Map.of("enabled", false);
                            case SERVER -> FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER ?
                                    null : Map.of("enabled", false);
                            default -> Map.of("enabled", false);
                        };
                    }
                    return null;
                }, mod);
            });
            m.onConfig(Utilities.cast(config));
            configs.put(m.getClass(), Lazy.of(() -> () -> config.build(false)));
        });
        return configs;
    }

    private void cleanConfigs() {
        Set<Path> paths = new HashSet<>();
        paths.add(FabricLoader.getInstance().getConfigDir().resolve("andromeda/mod.json"));
        configs.values().forEach(m -> paths.add(m.get().getSerializer().getPath()));
        Utilities.runUnchecked(() -> Files.walkFileTree(FabricLoader.getInstance().getConfigDir().resolve("andromeda"), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!paths.contains(file)) {
                    Files.delete(file);
                    LOGGER.info("Removed {} as it doesn't belong to any module!", file);
                }
                return super.visitFile(file, attrs);
            }
        }));
    }

    public ConfigManager<? extends BasicConfig> getConfig(Class<?> cls) {
        return MakeSure.notNull(configs.get(cls)).get();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getModule(Class<T> cls) {
        return (Optional<T>) Optional.ofNullable(modules.get(cls));
    }

    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getModule(String name) {
        return (Optional<T>) Optional.ofNullable(moduleNames.get(name));
    }

    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getDiscovered(Class<T> cls) {
        return (Optional<T>) Optional.ofNullable(discoveredModules.get(cls));
    }

    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getDiscovered(String name) {
        return (Optional<T>) Optional.ofNullable(discoveredModuleNames.get(name));
    }

    public Optional<Module<?>> moduleFromConfig(String name) {
        return Optional.ofNullable(mixinConfigs.get(name));
    }

    public Collection<Module<?>> all() {
        return discoveredModules.values();
    }

    public Collection<Module<?>> loaded() {
        return modules.values();
    }

    public static <T extends Module<?>> T quick(Class<T> cls) {
        return get().getModule(cls).orElseThrow(() -> new IllegalStateException("Module %s requested quickly, but is not loaded.".formatted(cls)));
    }

    public static ModuleManager get() {
        return MakeSure.notNull(Bootstrap.INSTANCE, "ModuleManager requested too early!");
    }

    public void print() {
        Map<String, Set<Module<?>>> categories = Utilities.consume(new LinkedHashMap<>(), map -> get().loaded().forEach(m ->
                map.computeIfAbsent(m.meta().category(), s -> new LinkedHashSet<>()).add(m)));

        StringBuilder builder = new StringBuilder();
        categories.forEach((s, strings) -> {
            builder.append("\n\t - ").append(s);
            if (!ModuleManager.categories.contains(s)) builder.append("*");
            builder.append("\n\t  |-- ");
            strings.forEach(m -> {
                builder.append('\'').append(m.meta().name().replace('/', '.')).append('\'').append("  ");
                if (!m.getClass().getName().startsWith("me.melontini.andromeda"))
                    builder.append('*');
            });
        });
        if (!categories.isEmpty()) {
            LOGGER.info("Loaded modules: {}", builder);
            LOGGER.info("* - custom modules/categories not provided by Andromeda.");
        } else {
            LOGGER.info("No modules loaded!");
        }
    }

    public interface ModuleSupplier {
        List<? extends Module<?>> get();
    }
}
