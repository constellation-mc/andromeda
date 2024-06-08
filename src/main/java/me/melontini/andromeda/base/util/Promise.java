package me.melontini.andromeda.base.util;

import me.melontini.andromeda.base.Module;
import me.melontini.dark_matter.api.base.util.Utilities;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public record Promise<T extends Module>(CompletableFuture<T> future, Module.Zygote zygote) {

    public Promise(Module.Zygote zygote) {
        this(new CompletableFuture<>(), zygote);
    }

    public void whenAvailable(Consumer<T> consumer) {
        future().whenComplete((t, throwable) -> {
            if (throwable == null) consumer.accept(t);
        });
    }

    public T get() {
        if (future.isDone()) return (T) zygote.supplier().get();
        throw new IllegalStateException("Module requested too early!");
    }

    public Class<T> type() {
        return Utilities.cast(zygote().type());
    }

    public Module.Metadata meta() {
        return zygote().meta();
    }
}
