package me.melontini.andromeda.base.events;

import me.melontini.andromeda.base.Module;

import java.util.HashMap;
import java.util.Map;

public interface ConstructorParametersEvent {

    Bus<ConstructorParametersEvent> BUS = new Bus<>(events -> (module) -> {
        Map<Class<?>, Object> map = new HashMap<>();
        events.forEach(event -> map.putAll(event.getAdditionalParameters(module)));
        return map;
    });

    Map<Class<?>, Object> getAdditionalParameters(Module module);
}
