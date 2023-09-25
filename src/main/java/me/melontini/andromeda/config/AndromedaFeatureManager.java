package me.melontini.andromeda.config;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

@CustomLog
public class AndromedaFeatureManager {

    private final static ThreadLocal<OptionProcessorRegistry<AndromedaConfig>> REGISTRY = ThreadLocal.withInitial(() -> null);

    @Deprecated
    public static void registerProcessor(String id, FeatureProcessor processor) {
        legacyRegister(id, processor::process);
        LOGGER.info("Using AndromedaFeatureManager is deprecated. Processor: " + id);
    }

    static void runLegacy(OptionProcessorRegistry<AndromedaConfig> registry) {
        try {
            REGISTRY.set(registry);
            EntrypointRunner.runEntrypoint("andromeda:feature_manager", Runnable.class, Runnable::run);
        } finally {
            REGISTRY.remove();
        }
    }

    private static void legacyRegister(String id, Function<AndromedaConfig, Map<String, Object>> processor) {
        String newId = id.split(":").length == 2 ? id : "legacy:" + id;
        REGISTRY.get().register(newId, manager -> processor.apply(manager.getConfig()));
    }

    public interface FeatureProcessor {
        @Nullable Map<String, Object> process(AndromedaConfig config);
    }
}
