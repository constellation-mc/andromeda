package me.melontini.andromeda.config;

public class ConfigSerializer implements me.shedaniel.autoconfig.serializer.ConfigSerializer<AndromedaConfig> {
    @Override
    public void serialize(AndromedaConfig andromeda) {
        ConfigHelper.writeConfigToFile(true);
    }

    @Override
    public AndromedaConfig deserialize() {
        return Config.get();
    }

    @Override
    public AndromedaConfig createDefault() {
        return new AndromedaConfig();
    }
}
