package me.melontini.andromeda.common.registries;

import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.classes.ThrowingSupplier;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class Common {

    public static void bootstrap(Class<?>... classes) {
        for (Class<?> cls : classes) {
            initKeepers(cls);
            Reflect.findMethod(cls, "init").ifPresent(m -> Utilities.runUnchecked(() -> m.invoke(null)));
        }
    }

    private static void initKeepers(@NotNull Class<?> reg) {
        for (Field field : reg.getFields()) {
            if (field.getType() != Keeper.class) continue;

            Keeper<?> keeper = (Keeper<?>) Utilities.supplyUnchecked(() -> field.get(reg));
            if (keeper.initialized()) throw new IllegalStateException("Registry object bootstrapped before the registry itself!");

            try {
                keeper.init();
            } catch (Throwable t) {
                throw new IllegalStateException("Failed to bootstrap registry object %s!".formatted(field.getName()), t);
            }
        }
    }

    public static <T, R extends ContentBuilder.CommonBuilder<T>> Keeper<T> start(Supplier<R> supplier) {
        R builder = supplier.get();
        return new Keeper<>(() -> builder::build);
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void bootstrap() {
        bootstrap(AndromedaItemGroup.class, ResourceRegistry.class);
    }

    public static <T> T run(ThrowingSupplier<T, Throwable> callable, String... features) {
        try {
            return callable.get();
        } catch (Throwable e) {
            throw new RuntimeException("Something went very wrong!", e);
        }
    }
}
