package me.melontini.andromeda.base.util;

import me.melontini.andromeda.base.Module;

import java.util.function.Consumer;

public interface Promise<T extends Module<?>> {
    void whenAvailable(Consumer<T> consumer);

    T get();

    Class<T> type();

    Module.Metadata meta();
}
