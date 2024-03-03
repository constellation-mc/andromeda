package me.melontini.andromeda.base.events;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;

public interface MixinConfigEvent {

    static <T extends Module.BaseConfig, M extends Module<T>> Bus<MixinConfigEvent> forModule(M module) {
        return module.getOrCreateBus("mixin_config_event", () -> new Bus<>(events -> manager -> events.forEach(event -> event.accept(manager))));
    }

    void accept(JsonObject config);
}
