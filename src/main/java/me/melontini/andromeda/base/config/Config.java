package me.melontini.andromeda.base.config;

import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;
import me.melontini.dark_matter.api.config.OptionManager;

@SuppressWarnings("UnstableApiUsage")
public class Config {

    private static final ConfigManager<AndromedaConfig> MANAGER = ConfigBuilder
            .create(AndromedaConfig.class, CommonValues.mod(), "andromeda/mod")
            .constructor(AndromedaConfig::new)
            .build();

    public static AndromedaConfig get() {
        return MANAGER.getConfig();
    }

    public static AndromedaConfig getDefault() {
        return MANAGER.getDefaultConfig();
    }

    public static OptionManager<AndromedaConfig> getOptionManager() {
        return MANAGER.getOptionManager();
    }

    public static void save() {
        MANAGER.save();
    }
}
