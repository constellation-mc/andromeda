package me.melontini.andromeda.api.config;

/**
 * Meant to be used with the {@code andromeda:features} entrypoint.
 */
public interface ProcessorCollector {

    void collect(ProcessorRegistry registry);
}
