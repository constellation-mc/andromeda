package me.melontini.andromeda.registries;

import me.melontini.dark_matter.api.base.util.MakeSure;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Keeper<T> {

    private Supplier<Callable<T>> supplier;
    private T value;

    public Keeper(Supplier<Callable<T>> supplier) {
        this.supplier = MakeSure.notNull(supplier);
    }

    public static <T> Keeper<T> of(Supplier<Callable<T>> supplier) {
        return new Keeper<>(supplier);
    }

    public boolean isPresent() {
        return value != null;
    }

    public void ifPresent(Consumer<T> consumer) {
        if (value != null) consumer.accept(value);
    }

    public boolean initialized() {
        return supplier == null;
    }

    void initialize() throws Exception {
        init();
    }

    private void init() throws Exception {
        value = supplier.get().call();
        supplier = null;
    }

    public T get() {
        return value;
    }
}
