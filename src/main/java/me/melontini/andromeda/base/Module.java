package me.melontini.andromeda.base;

import com.google.gson.JsonObject;
import lombok.CustomLog;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonOps;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.loader.api.FabricLoader;

@CustomLog
public abstract class Module<T extends BasicConfig> {

    private final Metadata info = Utilities.supply(() -> {
        ModuleInfo info1 = this.getClass().getAnnotation(ModuleInfo.class);
        if (info1 == null) throw new IllegalStateException("Module has no info!");
        return new Metadata(info1.name(), info1.category(), info1.environment());
    });

    ConfigManager<T> manager;
    T config;
    T defaultConfig;

    public void onClient() {
        initClasses("client.Client");
    }
    public void onServer() { }
    public void onMain() {
        initClasses("Main", "Content");
    }
    public void onPreLaunch() { }

    public void collectBlockades() {
    }

    protected final void initClasses(String... classes) {
        for (String cls : classes) {
            try {
                Common.bootstrap(this, Class.forName(this.getClass().getPackageName() + "." + cls));
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    public void onConfig(ConfigManager<T> manager) {
    }
    public void postConfig() { }

    public final Metadata meta() {
        return info;
    }

    public String mixins() {
        return this.getClass().getPackageName() + ".mixin";
    }
    public void acceptMixinConfig(JsonObject config) { }

    public final void save() {
        manager.save(FabricLoader.getInstance().getConfigDir(), config());
    }

    public final ConfigManager<T> manager() {
        return manager;
    }

    public final T config() {
        return config;
    }

    public final T defaultConfig() {
        return defaultConfig;
    }

    public final boolean enabled() {
        return config.enabled;
    }

    public void acceptLegacyConfig(JsonObject config) {
        if (this.getClass().isAnnotationPresent(OldConfigKey.class)) {
            OldConfigKey key = this.getClass().getAnnotation(OldConfigKey.class);
            if (config.has(key.value())) {
                JsonOps.ifPresent(config, key.value(), e -> this.config().enabled = e.getAsBoolean());
            }
        }
    }

    public record Metadata(String name, String category, Environment environment) {

        public String id() {
            return category() + "/" + name();
        }

        public String dotted() {
            return id().replace('/', '.');
        }
    }
}
