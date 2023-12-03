package me.melontini.andromeda.base;

import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
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
public class ModuleManager {

    private static final List<String> categories = List.of("world", "blocks", "entities", "items", "bugfixes", "mechanics", "gui", "misc");

    private final Set<Module<?>> discoveredModules = new LinkedHashSet<>();
    private final Map<Class<?>, Module<?>> modules = new LinkedHashMap<>();
    private final Map<String, Module<?>> moduleNames = new HashMap<>();
    private final Map<Class<?>, ConfigManager<? extends BasicConfig>> configs = new HashMap<>();
    final Map<String, String> mixinConfigs = new HashMap<>();

    public void prepare() {
        List<Module<?>> list = new ArrayList<>(Arrays.asList(ServiceLoader.load(Module.class)
                .stream().map(ServiceLoader.Provider::get).toArray(Module<?>[]::new)));

        EntrypointRunner.run("andromeda:modules", ModuleSupplier.class, s -> list.addAll(s.get()));

        list.removeIf(m -> (m.environment() == Environment.CLIENT && CommonValues.environment() == EnvType.SERVER));

        Set<String> ids = new HashSet<>();
        for (Module<?> module : list) {
            MakeSure.notEmpty(module.category(), "Module category can't be null or empty! Module: " + module.getClass());
            MakeSure.notEmpty(module.name(), "Module name can't be null or empty! Module: " + module.getClass());

            if (ids.contains(module.id())) throw new IllegalStateException("Duplicate module IDs! ID: %s, Module: %s".formatted(module.id(), module.getClass()));
            ids.add(module.id());
        }

        list.sort(Comparator.comparingInt(m-> {
            int i = categories.indexOf(m.category());
            return i >= 0 ? i : categories.size();
        }));
        list.forEach(m -> modules.put(m.getClass(), m));
        list.forEach(m -> moduleNames.put(m.id(), m));
        discoveredModules.addAll(modules.values());

        setUpConfigs(list);
        list.forEach(m -> m.manager().getOptionManager().processOptions());
        modules.values().removeIf(m -> !m.enabled());
        list.forEach(m -> m.manager().save());
    }

    public void setUpConfigs(List<Module<?>> list) {
        list.forEach(m -> {
            var config = ConfigBuilder.create(m.configClass(), CommonValues.mod(), "andromeda/" + m.id());
            config.processors((registry, mod) -> {
                registry.register(CommonValues.MODID + ":side_only_enabled", manager -> {
                    if (Config.get().sideOnlyMode) {
                        return switch (m.environment()) {
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
                m.onProcessors(Utilities.cast(registry), mod);
            });
            configs.put(m.getClass(), config.build(false));
        });

        Set<Path> paths = new HashSet<>();
        paths.add(FabricLoader.getInstance().getConfigDir().resolve("andromeda/mod.json"));
        configs.values().forEach(m -> paths.add(m.getSerializer().getPath()));
        Utilities.runUnchecked(() -> Files.walkFileTree(FabricLoader.getInstance().getConfigDir().resolve("andromeda"), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!paths.contains(file)) {
                    Files.delete(file);
                    AndromedaLog.info("Removed {} as it doesn't belong to any module!", file);
                }
                return super.visitFile(file, attrs);
            }
        }));
    }

    public ConfigManager<? extends BasicConfig> getConfig(Class<?> cls) {
        return configs.get(cls);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getModule(Class<T> cls) {
        return (Optional<T>) Optional.ofNullable(modules.get(cls)).filter(Module::enabled);
    }
    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getModule(String name) {
        return (Optional<T>) Optional.ofNullable(moduleNames.get(name)).filter(Module::enabled);
    }

    public Optional<Module<?>> moduleFromConfig(String name) {
        return Optional.ofNullable(mixinConfigs.get(name)).flatMap(this::getModule);
    }

    public Set<Module<?>> all() {
        return Collections.unmodifiableSet(discoveredModules);
    }
    public Collection<Module<?>> loaded() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public static  <T extends Module<?>> T quick(Class<T> cls) {
        return get().getModule(cls).orElseThrow(() -> new IllegalStateException("Module %s requested quickly, but is not loaded.".formatted(cls)));
    }

    public static ModuleManager get() {
        return MakeSure.notNull(Bootstrap.INSTANCE, "ModuleManager requested too early!");
    }

    public void print() {
        Map<String, Set<Module<?>>> categories = Utilities.consume(new LinkedHashMap<>(), map -> get().loaded().forEach(m ->
                map.computeIfAbsent(m.category(), s -> new LinkedHashSet<>()).add(m)));

        StringBuilder builder = new StringBuilder();
        categories.forEach((s, strings) -> {
            builder.append("\n\t - ").append(s);
            if (!ModuleManager.categories.contains(s)) builder.append("*");
            builder.append("\n\t  |-- ");
            strings.forEach(m -> {
                builder.append('\'').append(m.name().replace('/', '.')).append('\'').append("  ");
                if (!m.getClass().getName().startsWith("me.melontini.andromeda"))
                    builder.append('*');
            });
        });
        if (!categories.isEmpty()) {
            AndromedaLog.info("Loaded modules: {}", builder);
            AndromedaLog.info("* - custom modules/categories not provided by Andromeda.");
        } else {
            AndromedaLog.info("No modules loaded!");
        }
    }

    public interface ModuleSupplier {
        List<Module<?>> get();
    }
}
