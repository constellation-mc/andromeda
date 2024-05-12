package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.melontini.dark_matter.api.base.util.Context;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Locale;

@CustomLog
public class AndromedaConfig {

    private static final ConfigManager<Config> MANAGER = ConfigManager.of(Config.class, "andromeda/mod", Config::new)
            .exceptionHandler((e, stage, path) -> LOGGER.error("Failed to %s main Andromeda config (mod.json)!".formatted(stage.toString().toLowerCase(Locale.ROOT)), e));
    private static final Config CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir(), Context.of());
    private static final Config DEFAULT = MANAGER.createDefault();

    public static Config get() {
        return CONFIG;
    }

    public static Config getDefault() {
        return DEFAULT;
    }

    public static void save() {
        MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG, Context.of());
    }

    public static final class Config {
        @ConfigEntry.Gui.RequiresRestart
        public boolean sideOnlyMode = false;
        @ConfigEntry.Gui.RequiresRestart
        public boolean itemGroup = true;
        public boolean sendCrashReports = true;
    }
}
