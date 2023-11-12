package me.melontini.andromeda.base;

import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.util.annotations.config.Environment;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.ConfigManager;

public interface Module {

    default void onClient() { }
    default void onServer() { }
    default void onMain() { }
    default void onPreLaunch() { }

    default Environment environment() {
        return Environment.BOTH;
    }
    default Class<? extends BasicConfig> configClass() { return BasicConfig.class; }

    default <T extends BasicConfig> ConfigManager<T> manager() { return Utilities.cast(ModuleManager.get().getConfig(this.getClass())); }
    default <T extends BasicConfig> T config(Class<T> cls) {return Utilities.cast(manager().getConfig());}
    default <T extends BasicConfig> T config() {return Utilities.cast(manager().getConfig());}
    default boolean enabled() { return manager().get(boolean.class, "enable"); }
}
