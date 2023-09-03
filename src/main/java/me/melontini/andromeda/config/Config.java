package me.melontini.andromeda.config;

public class Config {

    private static AndromedaConfig CONFIG;

    public static AndromedaConfig set(AndromedaConfig config) {
        return CONFIG = config;
    }

    public static AndromedaConfig get() {
        return CONFIG;
    }
}
