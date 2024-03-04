package me.melontini.andromeda.base.events;

import me.melontini.andromeda.base.Module;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public interface InitEvent {

    static <T extends Module.BaseConfig, M extends Module<T>> Bus<InitEvent> main(M module) {
        return module.getOrCreateBus("main_init_event", () -> new Bus<>(events -> () -> {
            Set<Class<?>> classes = new LinkedHashSet<>();
            events.forEach(e -> classes.addAll(e.collect()));
            return classes;
        }));
    }

    static <T extends Module.BaseConfig, M extends Module<T>> Bus<InitEvent> client(M module) {
        return module.getOrCreateBus("client_init_event", () -> new Bus<>(events -> () -> {
            Set<Class<?>> classes = new LinkedHashSet<>();
            events.forEach(e -> classes.addAll(e.collect()));
            return classes;
        }));
    }

    static <T extends Module.BaseConfig, M extends Module<T>> Bus<InitEvent> server(M module) {
        return module.getOrCreateBus("server_init_event", () -> new Bus<>(events -> () -> {
            Set<Class<?>> classes = new LinkedHashSet<>();
            events.forEach(e -> classes.addAll(e.collect()));
            return classes;
        }));
    }

    Collection<Class<?>> collect();
}
