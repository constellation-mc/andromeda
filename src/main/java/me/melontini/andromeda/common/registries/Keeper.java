package me.melontini.andromeda.common.registries;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Keeper<T> {

    private volatile boolean initialized;

    private T value;

    private final Set<Consumer<T>> consumers = new HashSet<>();

    public static <T> Keeper<T> create() {
        return new Keeper<>();
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public void ifPresent(Consumer<T> consumer) {
        if (this.value != null) consumer.accept(this.value);
    }

    public Keeper<T> afterInit(Consumer<T> consumer) {
        this.consumers.add(consumer);
        return this;
    }

    public void init(T value) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    this.value = value;
                    this.initialized = true;

                    if (isPresent()) this.consumers.forEach(c -> c.accept(this.value));
                    this.consumers.clear();
                }
            }
        }
    }

    public T get() {
        return this.value;
    }

    public T orThrow() {
        return orThrow("No value present! Keeper not bootstrapped?");
    }

    public T orThrow(String msg) {
        return orThrow(() -> new IllegalStateException(msg));
    }

    public <X extends Throwable> T orThrow(Supplier<X> e) throws X {
        if (this.value == null) throw e.get();
        return this.value;
    }
}
