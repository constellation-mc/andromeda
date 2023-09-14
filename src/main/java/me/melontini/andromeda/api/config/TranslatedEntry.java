package me.melontini.andromeda.api.config;

public record TranslatedEntry(String key, Object... args) {

    public static final String DEFAULT_KEY = "andromeda.config.tooltip.manager.";

    public static TranslatedEntry withPrefix(String key,  Object... args) {
        return new TranslatedEntry(DEFAULT_KEY + key, args);
    }

    public static TranslatedEntry of(String key, Object... args) {
        return new TranslatedEntry(key, args);
    }
}
