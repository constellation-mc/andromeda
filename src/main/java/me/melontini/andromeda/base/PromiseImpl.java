package me.melontini.andromeda.base;

import me.melontini.andromeda.base.util.Promise;
import me.melontini.dark_matter.api.base.util.Utilities;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

record PromiseImpl<T extends Module<?>>(CompletableFuture<T> future, Module.Zygote zygote) implements Promise<T> {

    PromiseImpl(Module.Zygote zygote) {
        this(new CompletableFuture<>(), zygote);
    }

    @Override
    public void whenAvailable(Consumer<T> consumer) {
        future().whenComplete((t, throwable) -> {
            if (throwable == null) consumer.accept(t);
        });
    }

    @Override
    public T get() {
        if (future.isDone()) return (T) zygote.supplier().get();
        throw new IllegalStateException("Module requested too early!");
    }

    @Override
    public Class<T> type() {
        return Utilities.cast(zygote().type());
    }

    @Override
    public Module.Metadata meta() {
        return zygote().meta();
    }
}
