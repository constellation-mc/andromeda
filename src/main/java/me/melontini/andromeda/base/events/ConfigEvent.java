package me.melontini.andromeda.base.events;

import me.melontini.andromeda.base.Module;
import me.melontini.dark_matter.api.base.config.ConfigManager;

public interface ConfigEvent<T extends Module.BaseConfig> {

    static <T extends Module.BaseConfig, M extends Module<T>> Bus<ConfigEvent<T>> forModule(M module) {
        return module.getOrCreateBus(ConfigEvent.class, () -> new Bus<>(events -> manager -> events.forEach(event -> event.accept(manager))));
    }

    void accept(ConfigManager<T> manager);
}
