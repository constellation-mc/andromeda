package me.melontini.andromeda.base;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.registries.Common;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.classes.Lazy;
import me.melontini.dark_matter.api.config.ConfigManager;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import net.fabricmc.loader.api.ModContainer;

@SuppressWarnings("UnstableApiUsage")
public abstract class Module<T extends BasicConfig> {

    private final ModuleInfo info = Utilities.supply(() -> {
        ModuleInfo info1 = this.getClass().getAnnotation(ModuleInfo.class);
        if (info1 == null) throw new IllegalStateException("Module has no info!");
        return info1;
    });

    private final Lazy<ConfigManager<T>> manager = Lazy.of(() -> () -> Utilities.cast(ModuleManager.get().getConfig(this.getClass())));

    public void onClient() {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackageName() + ".client.Client");
            Common.bootstrap(cls);
        } catch (ClassNotFoundException ignored) { }
    }
    public void onServer() { }
    public void onMain() {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackageName() + ".Content");
            Common.bootstrap(cls);
        } catch (ClassNotFoundException ignored) { }
    }
    public void onPreLaunch() { }
    public void onProcessors(OptionProcessorRegistry<T> registry, ModContainer mod) { }

    public final Environment environment() {
        return info.environment();
    }
    public final String name() {
        return info.name();
    }
    public final String category() {
        return info.category();
    }
    public final String id() {
        return category() + "/" + name();
    }

    public String mixins() {
        return this.getClass().getPackageName() + ".mixin";
    }
    public void acceptMixinConfig(JsonObject config) { }

    public abstract Class<T> configClass();

    public final ConfigManager<T> manager() { return manager.get(); }
    public final T config() { return manager().getConfig(); }
    public boolean enabled() { return manager().get(boolean.class, "enabled"); }
}
