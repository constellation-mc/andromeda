package me.melontini.andromeda.base;

import com.google.gson.JsonObject;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonOps;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class for all modules.
 * <p> Modules are singletons, and are created by {@link Bootstrap}* during {@link Bootstrap.Status#DISCOVERY}.
 * <p> A module class must not contain any game classes or references in its fields, the constructor and config class, as they are loaded before the game.</p>
 * <p>Most methods must not be invoked directly.</p>
 *
 * @param <T> the config type for this module.
 */
@CustomLog
public abstract class Module<T extends Module.BaseConfig> {

    private final Metadata info;

    volatile ConfigManager<T> manager;
    volatile T config;
    volatile T defaultConfig;

    public Module() {
        ModuleInfo info1 = this.getClass().getAnnotation(ModuleInfo.class);
        if (info1 == null) throw new IllegalStateException("Module has no info!");
        this.info = new Metadata(info1.name(), info1.category(), info1.environment());
    }

    @ApiStatus.OverrideOnly
    public void onClient() {
        initClasses("client.Client");
    }

    @ApiStatus.OverrideOnly
    public void onServer() {
    }

    @ApiStatus.OverrideOnly
    public void onMerged() {
    }

    @ApiStatus.OverrideOnly
    public void onMain() {
        initClasses("Main", "Content");
    }

    @ApiStatus.OverrideOnly
    public void onPreLaunch() {
    }

    @ApiStatus.OverrideOnly
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

    @ApiStatus.OverrideOnly
    public void onConfig(ConfigManager<T> manager) {
    }

    @ApiStatus.OverrideOnly
    public void postConfig() { }

    public final Metadata meta() {
        return info;
    }

    @ApiStatus.OverrideOnly
    public String mixins() {
        return this.getClass().getPackageName() + ".mixin";
    }

    @ApiStatus.OverrideOnly
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

    @ApiStatus.OverrideOnly
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

    @Getter
    @Setter
    public static class BaseConfig {

        @ConfigEntry.Gui.RequiresRestart
        public boolean enabled = false;

        @ConfigEntry.Gui.Excluded
        public Scope scope = Scope.GLOBAL;

        public enum Scope {
            GLOBAL,
            WORLD,
            DIMENSION
        }
    }
}
