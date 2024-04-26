package me.melontini.andromeda.base.events;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;

public interface MixinConfigEvent {

    static <T extends Module.BaseConfig, M extends Module<T>> Bus<MixinConfigEvent> forModule(M module) {
        return module.getOrCreateBus("mixin_config_event", () -> new Bus<>(events -> (manager, object) -> events.forEach(event -> event.accept(manager, object))));
    }

    void accept(ModuleManager manager, JsonObject config);
}
