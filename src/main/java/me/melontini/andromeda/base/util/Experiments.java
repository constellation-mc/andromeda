package me.melontini.andromeda.base.util;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;

@CustomLog
public class Experiments {
    private static final ConfigManager<Config> MANAGER = ConfigManager.of(Config.class, "andromeda/experiments", Config::new)
            .exceptionHandler((e, stage, path) -> LOGGER.error("Failed to %s experiments config!".formatted(stage.toString().toLowerCase()), e));
    private static final Config CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
    private static final Config DEFAULT = MANAGER.createDefault();

    public static Config get() {
        return CONFIG;
    }

    public static Config getDefault() {
        return DEFAULT;
    }

    public static void save() {
        MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
    }

    public static class Config {

        @ConfigEntry.Gui.RequiresRestart
        public boolean scopedConfigs = false;
    }
}
