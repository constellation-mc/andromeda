package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import org.intellij.lang.annotations.MagicConstant;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@CustomLog
@SuppressWarnings("UnstableApiUsage")
public class Debug {

    private static final ConfigManager<Holder> MANAGER = ConfigBuilder
            .create(Holder.class, CommonValues.mod(), "andromeda/debug")
            .constructor(Holder::new)
            .postSave(manager -> {
                if (manager.getConfig().keys.contains(Keys.PRINT_DEBUG_KEYS)) {
                    LOGGER.info(Arrays.toString(manager.getConfig().keys.toArray()));
                }
            })
            .build();

    public static boolean hasKey(@MagicConstant(valuesFromClass = Keys.class) String key) {
        return MANAGER.getConfig().keys.contains(key);
    }

    public static void load() {
        MANAGER.load();
    }

    private static class Holder {
        Set<String> keys = new LinkedHashSet<>();

        Holder() {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                this.keys.add(Keys.PRINT_DEBUG_MESSAGES);
                this.keys.add(Keys.VERIFY_MIXINS);
                this.keys.add(Keys.DISPLAY_TRACKED_VALUES);
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
    }
}
