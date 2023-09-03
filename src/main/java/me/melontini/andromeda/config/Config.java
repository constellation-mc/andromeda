package me.melontini.andromeda.config;

public class Config {

    private static AndromedaConfig CONFIG;

    static void set(AndromedaConfig config) {
        CONFIG = config;
    }

    public static AndromedaConfig get() {
        return CONFIG;
    }
}
