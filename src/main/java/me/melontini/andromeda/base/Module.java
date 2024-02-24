package me.melontini.andromeda.base;

import com.google.common.base.Suppliers;
import lombok.*;
import lombok.experimental.Accessors;
import me.melontini.andromeda.base.events.Bus;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Base class for all modules.
 * <p> Modules are singletons, and are created by {@link ModuleManager}* during {@link Bootstrap.Status#SETUP}.
 * <p> A module class must not contain any game classes or references in its fields, the constructor and config class, as they are loaded before the game.</p>
 * <p>The only functional part of a module is its constructor.</p>
 *
 * @param <T> the config type for this module.
 */
@CustomLog
public abstract class Module<T extends Module.BaseConfig> {

    private final Metadata info;

    volatile ConfigManager<T> manager;
    volatile T config;
    volatile T defaultConfig;

    private final Map<Class<?>, Bus<?>> busMap = new IdentityHashMap<>();

    protected Module() {
        this.info = Metadata.fromAnnotation(this.getClass().getAnnotation(ModuleInfo.class));
    }

    public final Metadata meta() {
        return info;
    }

    public final void save() {
        manager.save(FabricLoader.getInstance().getConfigDir(), config());
    }

    public final ConfigManager<T> manager() {
        return manager;
    }

    public final T config() {
        return config;
    }

    public final T defaultConfig() {
        return defaultConfig;
    }

    public final boolean enabled() {
        return config.enabled;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "info=" + info + '}';
    }

    @ApiStatus.Internal
    public <E> Bus<E> getOrCreateBus(Class<?> type, Supplier<Bus<E>> supplier) {
        return (Bus<E>) busMap.computeIfAbsent(type, aClass -> supplier == null ? null : supplier.get());
    }

    @SneakyThrows
    final void initClasses(String str) {
        try {
            var cls = Class.forName(this.getClass().getPackageName() + "." + str, false, this.getClass().getClassLoader());
            MakeSure.isTrue(cls.getDeclaredConstructors().length == 1);
            var ctx = Reflect.setAccessible(cls.getDeclaredConstructors()[0]);

            if (ctx.getParameterCount() == 0) {
                AndromedaException.run(ctx::newInstance, b -> b.message("Failed to construct module class!").add("class", str));
            } else {
                Map<Class<?>, Object> args = Map.of(
                        this.getClass(), this,
                        ModuleManager.get().getConfigClass(this.getClass()), this.config()
                );

                List<Object> passed = new ArrayList<>(ctx.getParameterCount());
                for (Class<?> parameterType : ctx.getParameterTypes()) {
                    var value = MakeSure.notNull(args.get(parameterType));
                    passed.add(value);
                }
                AndromedaException.run(() -> ctx.newInstance(passed.toArray(Object[]::new)), b -> b.message("Failed to construct module class!").add("class", str));
            }
        } catch (ClassNotFoundException ignored) {
        }
    }

    public record Metadata(String name, String category, Environment environment) {

        public static Metadata fromAnnotation(ModuleInfo info) {
            return new Metadata(info.name(), info.category(), info.environment());
        }

        public String id() {
            return category() + "/" + name();
        }

        public String dotted() {
            return id().replace('/', '.');
        }
    }

    @Getter
    @Setter
    public static class BaseConfig {

        @ConfigEntry.Gui.RequiresRestart
        public boolean enabled = false;

        @ConfigEntry.Gui.Excluded
        public Scope scope = Scope.GLOBAL;

        public enum Scope {
            GLOBAL, WORLD, DIMENSION
        }
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    public static class Zygote {
        Class<?> type;
        Metadata meta;
        Supplier<? extends Module<?>> supplier;

        public static Zygote spawn(Class<?> type, Supplier<? extends Module<?>> supplier) {
            ModuleInfo info = type.getAnnotation(ModuleInfo.class);
            if (info == null) throw new IllegalStateException("Module has no info!");

            return new Zygote(type, Metadata.fromAnnotation(info), Suppliers.memoize(supplier::get));
        }
    }
}
