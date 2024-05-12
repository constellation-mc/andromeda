package me.melontini.andromeda.base;

import com.google.common.base.Suppliers;
import lombok.*;
import lombok.experimental.Accessors;
import me.melontini.andromeda.base.events.Bus;
import me.melontini.andromeda.base.events.ConstructorParametersEvent;
import me.melontini.andromeda.base.util.BootstrapConfig;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Base class for all modules.
 * <p> Modules are singletons, and are created by {@link ModuleManager}* during {@link Bootstrap.Status#SETUP}.
 * <p> A module class must not contain any game classes or references in its fields, the constructor and config class, as they are loaded before the game.</p>
 * <p>The only functional part of a module is its constructor.</p>
 */
@CustomLog @Accessors(fluent = true)
public abstract class Module {

    private final Metadata info;
    @Getter
    private final PrependingLogger logger;

    private final Map<String, Bus<?>> busMap = new HashMap<>();
    private final IdentityHashMap<Class<?>, Object> objectMap = new IdentityHashMap<>();
    private final EnumMap<ConfigState, ConfigDefinition<?>> configs = new EnumMap<>(ConfigState.class);

    protected Module() {
        this.info = Metadata.fromAnnotation(this.getClass().getAnnotation(ModuleInfo.class));
        this.logger = PrependingLogger.get("Andromeda/" + meta().id(), PrependingLogger.LOGGER_NAME);
    }

    protected void defineConfig(ConfigState state, ConfigDefinition<?> supplier) {
        this.configs.put(state, supplier);
    }

    public ConfigDefinition<?> getConfigDefinition(ConfigState state) {
        return this.configs.get(state);
    }

    public final Metadata meta() {
        return info;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "info=" + info + '}';
    }

    @ApiStatus.Internal
    public <E> Bus<E> getOrCreateBus(String id, @Nullable Supplier<Bus<E>> supplier) {
        return (Bus<E>) busMap.computeIfAbsent(id, aClass -> supplier == null ? null : supplier.get());
    }

    public <O> O getObject(Class<O> cls) {
        return cls.cast(this.objectMap.get(cls));
    }

    @SneakyThrows
    final void initClass(Class<?> cls) {
        MakeSure.isTrue(cls.getDeclaredConstructors().length == 1);
        var ctx = Reflect.setAccessible(cls.getDeclaredConstructors()[0]);

        if (ctx.getParameterCount() == 0) {
            AndromedaException.run(() -> this.objectMap.put(cls, ctx.newInstance()), b -> b.literal("Failed to construct module class!").add("class", cls.getName()));
        } else {
            Map<Class<?>, Object> args = new HashMap<>(Map.of(
                    this.getClass(), this,
                    BootstrapConfig.class, ModuleManager.get().getConfig(this)
            ));
            args.putAll(ConstructorParametersEvent.BUS.invoker().getAdditionalParameters(this));

            List<Object> passed = new ArrayList<>(ctx.getParameterCount());
            for (Class<?> parameterType : ctx.getParameterTypes()) {
                var value = Objects.requireNonNull(args.get(parameterType), cls.getName());
                passed.add(value);
            }
            AndromedaException.run(() -> this.objectMap.put(cls, ctx.newInstance(passed.toArray(Object[]::new))), b -> b.literal("Failed to construct module class!")
                    .add("parameters", ctx.getParameterTypes())
                    .add("args", passed)
                    .add("class", cls.getName()));
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

    public static class BaseConfig { }

    public static class GameConfig extends BaseConfig {
        @ConfigEntry.Gui.Excluded
        public BooleanIntermediary available = BooleanIntermediary.of(true);
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    public static class Zygote {
        Class<?> type;
        Metadata meta;
        Supplier<? extends Module> supplier;

        public static Zygote spawn(Class<?> type, Supplier<? extends Module> supplier) {
            ModuleInfo info = type.getAnnotation(ModuleInfo.class);
            if (info == null) throw new IllegalStateException("Module has no info!");

            return new Zygote(type, Metadata.fromAnnotation(info), Suppliers.memoize(supplier::get));
        }
    }
}
