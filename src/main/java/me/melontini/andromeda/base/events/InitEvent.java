package me.melontini.andromeda.base.events;

import me.melontini.andromeda.base.Module;

import java.util.LinkedHashSet;
import java.util.Set;

public interface InitEvent {

    static <M extends Module> Bus<InitEvent> main(M module) {
        return baseEvent("main", module);
    }

    static <M extends Module> Bus<InitEvent> client(M module) {
        return baseEvent("client", module);
    }

    static <M extends Module> Bus<InitEvent> server(M module) {
        return baseEvent("server", module);
    }

    static <M extends Module> Bus<InitEvent> baseEvent(String env, M module) {
        return module.getOrCreateBus(env + "_init_event", () -> new Bus<>(events -> () -> {
            Set<Runnable> classes = new LinkedHashSet<>();
            events.forEach(e -> classes.add(e.collectInits()));
            return () -> classes.forEach(Runnable::run);
        }));
    }

    Runnable collectInits();
}
