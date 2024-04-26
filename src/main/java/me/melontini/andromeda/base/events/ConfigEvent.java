package me.melontini.andromeda.base.events;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.dark_matter.api.base.config.ConfigManager;

public interface ConfigEvent<T extends Module.BaseConfig> {

    static <T extends Module.BaseConfig, M extends Module<T>> Bus<ConfigEvent<T>> forModule(M module) {
        return module.getOrCreateBus("config_event", () -> new Bus<>(events -> (manager, configManager) -> events.forEach(event -> event.accept(manager, configManager))));
    }

    void accept(ModuleManager manager, ConfigManager<T> configManager);
}
