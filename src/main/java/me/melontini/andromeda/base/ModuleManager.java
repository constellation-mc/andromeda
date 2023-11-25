package me.melontini.andromeda.base;

import com.google.common.base.Suppliers;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ModuleManager {

    private static final Supplier<ModuleManager> INSTANCE = Suppliers.memoize(ModuleManager::new);

    private final Set<Module<?>> discoveredModules = new LinkedHashSet<>();
    private final Map<Class<?>, Module<?>> modules = new LinkedHashMap<>();
    private final Map<String, Module<?>> moduleNames = new HashMap<>();
    private final Map<Class<?>, ConfigManager<? extends BasicConfig>> configs = new HashMap<>();
    private static final List<String> categories = List.of("world", "blocks", "entities", "items", "bugfixes", "mechanics", "gui", "misc");

    public void prepare() {
        List<Module<?>> list = new ArrayList<>(Arrays.asList(ServiceLoader.load(Module.class)
                .stream().map(ServiceLoader.Provider::get).toArray(Module<?>[]::new)));

        list.removeIf(m -> (m.environment() == Environment.CLIENT && CommonValues.environment() == EnvType.SERVER));

        Set<String> ids = new HashSet<>();
        for (Module<?> module : list) {
            if (module.category() == null || module.category().isBlank()) throw new IllegalStateException("Module category can't be null or empty! Module: " + module.getClass());
            if (module.name() == null || module.name().isBlank()) throw new IllegalStateException("Module name can't be null or empty! Module: " + module.getClass());

            if (ids.contains(module.id())) throw new IllegalStateException("Duplicate module IDs! ID: %s, Module: %s".formatted(module.id(), module.getClass()));
            ids.add(module.id());
        }

        list.sort(Comparator.comparingInt(m->categories.indexOf(m.category())));
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
        return INSTANCE.get();
    }

    public static void onClient() {
        get().modules.values().forEach(Module::onClient);
        AndromedaClient.init();
    }

    public static void onServer() {
        get().modules.values().forEach(Module::onServer);
    }

    public static void onMain() {
        get().modules.values().forEach(Module::onMain);
        Andromeda.init();
    }

    public static void onPreLaunch() {
        get().modules.values().forEach(Module::onPreLaunch);
    }

    public void print() {
        StringBuilder builder = new StringBuilder();
        modules.values().forEach(m -> builder.append(m.id()).append(", "));
        AndromedaLog.info("Loading modules: {}", builder);
    }
}
