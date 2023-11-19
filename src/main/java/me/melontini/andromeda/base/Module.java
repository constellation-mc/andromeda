package me.melontini.andromeda.base;

import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.registries.Common;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.ConfigManager;

@SuppressWarnings("UnstableApiUsage")
public interface Module<T extends BasicConfig> {

    default void onClient() {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackageName() + ".client.Client");
            Common.bootstrap(cls);
        } catch (ClassNotFoundException ignored) { }
    }
    default void onServer() { }
    default void onMain() {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackageName() + ".Content");
            Common.bootstrap(cls);
        } catch (ClassNotFoundException ignored) { }
    }
    default void onPreLaunch() { }

    default Environment environment() {
        FeatureEnvironment env = this.getClass().getAnnotation(FeatureEnvironment.class);
        if (env != null) return env.value();
        return Environment.BOTH;
    }

    Class<T> configClass();

    default ConfigManager<T> manager() { return Utilities.cast(ModuleManager.get().getConfig(this.getClass())); }
    default T config() {return Utilities.cast(manager().getConfig());}
    default boolean enabled() { return manager().get(boolean.class, "enabled"); }
}
