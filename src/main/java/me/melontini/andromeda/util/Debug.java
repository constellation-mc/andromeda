package me.melontini.andromeda.util;

import lombok.CustomLog;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.mixin.ErrorHandler;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import org.intellij.lang.annotations.MagicConstant;

import java.util.*;

@CustomLog
public class Debug {

    private static final ConfigManager<Holder> MANAGER = ConfigManager.of(Holder.class, "andromeda/debug", Holder::new)
            .exceptionHandler((e, stage, path) -> LOGGER.error("Failed to %s debug config!".formatted(stage.toString().toLowerCase()), e));

    private static Holder CONFIG;

    public static boolean hasKey(@MagicConstant(valuesFromClass = Keys.class) String key) {
        return CONFIG.keys.contains(key);
    }

    public static boolean skipIntegration(String m, String key) {
        return CONFIG.skipModIntegration.getOrDefault(m, Collections.emptySet()).contains(key);
    }

    public static void load() {
        CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
        MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
    }

    private static class Holder {
        Set<String> keys = new LinkedHashSet<>();
        Map<String, Set<String>> skipModIntegration = new HashMap<>();

        Holder() {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                this.keys.add(Keys.PRINT_DEBUG_MESSAGES);
                this.keys.add(Keys.VERIFY_MIXINS);
                this.keys.add(Keys.DISPLAY_TRACKED_VALUES);
                this.keys.add(Keys.PRINT_MISSING_ASSIGNED_DATA);
            }
        }
    }

    public static class Keys {
        /**
         * Some minor mixin sanity checks. Verifies modifiers and forces an environment audit on startup.
         */
        public static final String VERIFY_MIXINS = "verifyMixins";
        public static final String PRINT_DEBUG_MESSAGES = "printDebugMessages";
        /**
         * Skips the {@link ErrorHandler}. This handler reports mixin crashes and disables modules.
         */
        public static final String SKIP_MIXIN_ERROR_HANDLER = "skipMixinErrorHandler";
        /**
         * Skips the server-side module check.
         */
        public static final String SKIP_SERVER_MODULE_CHECK = "skipServerModuleCheck";
        public static final String DISPLAY_TRACKED_VALUES = "displayTrackedValues";
        /**
         * Force enables all modules.
         */
        public static final String ENABLE_ALL_MODULES = "enableAllModules";
        /**
         * Forces all configs to be in {@link Module.BaseConfig.Scope#DIMENSION}.
         */
        public static final String FORCE_DIMENSION_SCOPE = "forceDimensionScope";
        /**
         * Prints missing data for blocks, items, etc. When there should be some. Case in point: {@code world/crop_temperature}
         */
        public static final String PRINT_MISSING_ASSIGNED_DATA = "printMissingAssignedData";
        /**
         * Prints missing option tooltips for the currently selected language after client resources are loaded.
         */
        public static final String PRINT_MISSING_TOOLTIPS = "printMissingTooltips";
        public static final String FORCE_CRASH_REPORT_UPLOAD = "forceCrashReportUpload";
    }
}
