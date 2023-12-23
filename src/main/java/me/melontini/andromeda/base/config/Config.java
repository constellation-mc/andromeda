package me.melontini.andromeda.base.config;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;

@CustomLog
public class Config {

    private static final ConfigManager<AndromedaConfig> MANAGER = ConfigManager.of(AndromedaConfig.class, "andromeda/mod", AndromedaConfig::new)
            .exceptionHandler((e, stage) -> LOGGER.error("Failed to %s main Andromeda config (mod.json)!".formatted(stage.toString().toLowerCase()), e));
    private static AndromedaConfig CONFIG;
    private static AndromedaConfig DEFAULT;

    public static void load() {
        CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
        DEFAULT = MANAGER.createDefault();
        MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
    }

    public static AndromedaConfig get() {
        return CONFIG;
    }

    public static AndromedaConfig getDefault() {
        return DEFAULT;
    }

    public static void save() {
        MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
    }
}
