package me.melontini.andromeda.registries;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.MakeSure;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Keeper<T> {

    private Supplier<Callable<T>> supplier;
    private T value;
    private Feature feature;

    public Keeper(Supplier<Callable<T>> supplier) {
        this.supplier = MakeSure.notNull(supplier);
    }

    public static <T> Keeper<T> of(Supplier<Callable<T>> supplier) {
        return new Keeper<>(supplier);
    }

    public void ifPresent(Consumer<T> consumer) {
        if (value != null) consumer.accept(value);
    }

    public boolean initialized() {
        return supplier == null;
    }

    void initialize(Feature f) throws Exception {
        feature = f;
        if (f != null) {
            if (Config.get(f.value())) init();
        } else init();
    }

    private void init() throws Exception {
        value = supplier.get().call();
        supplier = null;
    }

    public Optional<Feature> getFeature() {
        return Optional.ofNullable(feature);
    }

    public T get() {
        return value;
    }
}
