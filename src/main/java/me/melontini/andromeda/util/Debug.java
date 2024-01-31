package me.melontini.andromeda.util;

import lombok.CustomLog;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.mixin.ErrorHandler;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;

import java.util.*;

@CustomLog
public class Debug {

    private static final Map<String, Key> registry = new HashMap<>();

    private static final ConfigManager<Holder> MANAGER = ConfigManager.of(Holder.class, "andromeda/debug", Holder::new)
            .onSave((config, path) -> registry.forEach((string, key) -> key.isPresent = config.keys.contains(key.getKey())))
            .exceptionHandler((e, stage, path) -> LOGGER.error("Failed to %s debug config!".formatted(stage.toString().toLowerCase()), e));

    private static Holder CONFIG;

    public static boolean skipIntegration(String module, String mod) {
        return CONFIG.skipModIntegration.getOrDefault(module, Collections.emptySet()).contains(mod);
    }

    public static void load() {
        CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
        MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
    }

    @Getter
    public static class Key {
        @Accessors(fluent = true)
        private boolean isPresent = false;
        private final String key;

        public Key(String key) {
            this.key = key;
        }
    }

    private static class Holder {
        Set<String> keys = new LinkedHashSet<>();
        Map<String, Set<String>> skipModIntegration = new HashMap<>();

        Holder() {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                this.keys.add(Keys.PRINT_DEBUG_MESSAGES.getKey());
                this.keys.add(Keys.VERIFY_MIXINS.getKey());
                this.keys.add(Keys.DISPLAY_TRACKED_VALUES.getKey());
                this.keys.add(Keys.PRINT_MISSING_ASSIGNED_DATA.getKey());
            }
        }
    }

    private static Key make(String string) {
        var l = new Key(string);
        registry.put(string, l);
        return l;
    }

    public static class Keys {
        /**
         * Some minor mixin sanity checks. Verifies modifiers and forces an environment audit on startup.
         */
        public static final Key VERIFY_MIXINS = make("verifyMixins");
        public static final Key PRINT_DEBUG_MESSAGES = make("printDebugMessages");
        /**
         * Skips the {@link ErrorHandler}. This handler reports mixin crashes and disables modules.
         */
        public static final Key SKIP_MIXIN_ERROR_HANDLER = make("skipMixinErrorHandler");
        /**
         * Skips the server-side module check.
         */
        public static final Key SKIP_SERVER_MODULE_CHECK = make("skipServerModuleCheck");
        public static final Key DISPLAY_TRACKED_VALUES = make("displayTrackedValues");
        /**
         * Force enables all modules.
         */
        public static final Key ENABLE_ALL_MODULES = make("enableAllModules");
        /**
         * Forces all configs to be in {@link Module.BaseConfig.Scope#DIMENSION}.
         */
        public static final Key FORCE_DIMENSION_SCOPE = make("forceDimensionScope");
        /**
         * Prints missing data for blocks, items, etc. When there should be some. Case in point: {@code world/crop_temperature}
         */
        public static final Key PRINT_MISSING_ASSIGNED_DATA = make("printMissingAssignedData");
        /**
         * Prints missing option tooltips for the currently selected language after client resources are loaded.
         */
        public static final Key PRINT_MISSING_TOOLTIPS = make("printMissingTooltips");
        public static final Key FORCE_CRASH_REPORT_UPLOAD = make("forceCrashReportUpload");
    }
}
