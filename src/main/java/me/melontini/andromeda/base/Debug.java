package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import org.intellij.lang.annotations.MagicConstant;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@CustomLog
public class Debug {

    private static final ConfigManager<Holder> MANAGER = ConfigManager.of(Holder.class, "andromeda/debug", Holder::new)
            .onSave(ConfigManager.State.POST, config -> {
                if (config.keys.contains(Keys.PRINT_DEBUG_KEYS)) {
                    LOGGER.info(Arrays.toString(config.keys.toArray()));
                }
            });

    private static Holder CONFIG;

    public static boolean hasKey(@MagicConstant(valuesFromClass = Keys.class) String key) {
        return CONFIG.keys.contains(key);
    }

    public static void load() {
        try {
            CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
        } catch (IOException e) {
            LOGGER.error("Failed to load debug keys!");
        }
    }

    private static class Holder {
        Set<String> keys = new LinkedHashSet<>();

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
        public static final String PRINT_DEBUG_KEYS = "printDebugKeys";
        public static final String VERIFY_MIXINS = "verifyMixins";
        public static final String PRINT_DEBUG_MESSAGES = "printDebugMessages";
        public static final String SKIP_MIXIN_ERROR_HANDLER = "skipMixinErrorHandler";
        public static final String SKIP_SERVER_MODULE_CHECK = "skipServerModuleCheck";
        public static final String DISPLAY_TRACKED_VALUES = "displayTrackedValues";
        public static final String ENABLE_ALL_MODULES = "enableAllModules";
        public static final String FORCE_DIMENSION_SCOPE = "forceDimensionScope";
        public static final String PRINT_MISSING_ASSIGNED_DATA = "printMissingAssignedData";
    }
}
