package me.melontini.andromeda.base.config;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;

@CustomLog
public class Config {

    private static final ConfigManager<AndromedaConfig> MANAGER = ConfigManager.of(AndromedaConfig.class, "andromeda/mod", AndromedaConfig::new);
    private static AndromedaConfig CONFIG;
    private static AndromedaConfig DEFAULT;

    public static void load() {
        try {
            CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
        } catch (IOException e) {
            LOGGER.error("Failed to load main Andromeda config (mod.json)!", e);
            CONFIG = MANAGER.createDefault();
        }
        DEFAULT = MANAGER.createDefault();
        try {
            MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
        } catch (IOException e) {
            LOGGER.error("Failed to save main Andromeda config (mod.json)!", e);
        }
    }

    public static AndromedaConfig get() {
        return CONFIG;
    }

    public static AndromedaConfig getDefault() {
        return DEFAULT;
    }

    public static void save() {
        try {
            MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG);
        } catch (IOException e) {
            LOGGER.error("Failed to save main Andromeda config (mod.json)!", e);
        }
    }
}
