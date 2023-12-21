package me.melontini.andromeda.base.config;

import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;

public class Config {

    private static final ConfigManager<AndromedaConfig> MANAGER = ConfigManager.of(AndromedaConfig.class, "andromeda/mod", AndromedaConfig::new);
    private static AndromedaConfig CONFIG;
    private static AndromedaConfig DEFAULT;

    public static void load() {
        try {
            CONFIG = MANAGER.load(FabricLoader.getInstance().getConfigDir());
            DEFAULT = MANAGER.createDefault();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load main Andromeda config (mod.json)!");
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
            throw new RuntimeException("Failed to save main Andromeda config (mod.json)!", e);
        }
    }
}
