package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;

@CustomLog
public class AndromedaConfig {

    private static final ConfigManager<Config> MANAGER = ConfigManager.of(Config.class, "andromeda/mod", Config::new)
            .exceptionHandler((e, stage, path) -> LOGGER.error("Failed to %s main Andromeda config (mod.json)!".formatted(stage.toString().toLowerCase()), e));
    private static Config CONFIG;
    private static Config DEFAULT;

    public static void load() {
        CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
        DEFAULT = MANAGER.createDefault();
        MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
    }

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
        public boolean sideOnlyMode = false;

        public boolean sendCrashReports = true;
    }
}
