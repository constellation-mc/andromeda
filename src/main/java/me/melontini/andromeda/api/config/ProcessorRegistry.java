package me.melontini.andromeda.api.config;

import me.melontini.andromeda.config.AndromedaConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public interface ProcessorRegistry {

    void register(String id, Function<AndromedaConfig, Map<String, Object>> processor);

    void register(String id, Function<AndromedaConfig, Map<String, Object>> processor, Function<String, TranslatedEntry> reason);

    <T> @Nullable T get(String feature);
}
