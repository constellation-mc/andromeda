package me.melontini.andromeda.common.registries;

import lombok.CustomLog;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Supplier;

import static me.melontini.andromeda.util.CommonValues.MODID;

@CustomLog
public class Common {

    //DO NOT CALL THIS FROM THE WRONG MODULE!!!
    public static void bootstrap(Module<?> module, Class<?>... classes) {
        AndromedaException.run(() -> {
            MakeSure.notNull(module);

            for (Class<?> cls : classes) {
                Reflect.findField(cls, "MODULE").ifPresent(field -> Exceptions.run(() -> {
                    MakeSure.isTrue(field.getType() == module.getClass(), "Illegal module field type '%s'! Must be '%s'".formatted(field.getType(), module.getClass()));
                    field.setAccessible(true);
                    LOGGER.debug("Setting module field for class '{}' to module '{}'", cls, module.meta().id());
                    field.set(null, module);
                }));

                initKeepers(cls);

                Reflect.findMethod(cls, "init", module.getClass()).ifPresent(m -> Exceptions.run(() -> m.invoke(null, module)));
                Reflect.findMethod(cls, "init").ifPresent(m -> Exceptions.run(() -> m.invoke(null)));
            }
        }, () -> new AndromedaException.Builder()
                .message("Failed to bootstrap module!")
                .add("module", module.meta().id()).add("classes", Arrays.toString(classes)));
    }

    private static void initKeepers(@NotNull Class<?> reg) {
        for (Field field : reg.getFields()) {
            if (field.getType() != Keeper.class || !Modifier.isStatic(field.getModifiers())) continue;

            Keeper<?> keeper = (Keeper<?>) Exceptions.supply(() -> field.get(reg));
            if (keeper.initialized()) throw new IllegalStateException("Registry object bootstrapped before the registry itself!");

            try {
                LOGGER.debug("Initializing Keeper {} for class {}", field.getName(), reg.getName());
                keeper.init(field);
            } catch (Throwable t) {
                throw new IllegalStateException("Failed to bootstrap registry object %s!".formatted(field.getName()), t);
            }
        }
    }

    public static <T, R extends ContentBuilder.CommonBuilder<T>> Keeper<T> start(Supplier<R> supplier) {
        return new Keeper<>(() -> supplier.get().build());
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void bootstrap() {
        bootstrap(AndromedaItemGroup.class, ResourceRegistry.class);
    }

    private static void bootstrap(Class<?>... classes) {
        for (Class<?> cls : classes) {
            Reflect.findMethod(cls, "init").ifPresent(m -> Exceptions.run(() -> m.invoke(null)));
            initKeepers(cls);
        }
    }
}
