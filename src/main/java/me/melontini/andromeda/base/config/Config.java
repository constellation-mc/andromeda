package me.melontini.andromeda.base.config;

import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;
import me.melontini.dark_matter.api.config.OptionManager;
import me.melontini.dark_matter.api.config.interfaces.TextEntry;

import java.util.Arrays;

@SuppressWarnings("UnstableApiUsage")
public class Config {

    static final String DEFAULT_KEY = "andromeda.config.tooltip.manager.";

    private static final ConfigManager<AndromedaConfig> MANAGER = ConfigBuilder
            .create(AndromedaConfig.class, CommonValues.mod(), "andromeda")
            .constructor(AndromedaConfig::new)
            .defaultReason(holder -> {
                if ("andromeda:custom_values".equals(holder.processor().id())) {
                    return TextEntry.translatable(DEFAULT_KEY + "mod_json", Arrays.toString(Config.getOptionManager().blameModJson(holder.field()).right().toArray()));
                }
                return TextEntry.translatable(DEFAULT_KEY + holder.processor().id().replace(":", "."));
            })
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
