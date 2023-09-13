package me.melontini.andromeda.config;

public class Config {

    private static AndromedaConfig CONFIG;

    static void set(AndromedaConfig config) {
        CONFIG = config;
    }

    public static AndromedaConfig get() {
        return CONFIG;
    }

    public static AndromedaConfig getDefault() {
        return Default.DEFAULT;
    }

    private static class Default {

        static final AndromedaConfig DEFAULT = new AndromedaConfig();
    }
}
