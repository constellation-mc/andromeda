package me.melontini.andromeda.config;

import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.dark_matter.api.base.util.classes.ThrowingRunnable;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.Callable;

@SuppressWarnings("UnstableApiUsage")
public class Config {

    private static final ConfigManager<AndromedaConfig> MANAGER = ConfigBuilder
            .create(AndromedaConfig.class, SharedConstants.MOD_CONTAINER, "andromeda")
            .fixups(Fixup::addFixups)
            .redirects(builder -> builder
                    .add("throwableItems", "throwableItems.enable")
                    .add("throwableItemsBlacklist", "throwableItems.blacklist")
                    .add("incubatorSettings.enableIncubator", "incubator.enable")
                    .add("incubatorSettings.incubatorRandomness", "incubator.randomness")
                    .add("incubatorSettings.incubatorRecipe", "incubator.recipe")
                    .add("autogenRecipeAdvancements.autogenRecipeAdvancements", "recipeAdvancementsGeneration.enable")
                    .add("autogenRecipeAdvancements.blacklistedRecipeNamespaces", "recipeAdvancementsGeneration.namespaceBlacklist")
                    .add("autogenRecipeAdvancements.blacklistedRecipeIds", "recipeAdvancementsGeneration.recipeBlacklist")
                    .add("campfireTweaks.campfireEffects", "campfireTweaks.effects")
                    .add("campfireTweaks.campfireEffectsPassive", "campfireTweaks.affectsPassive")
                    .add("campfireTweaks.campfireEffectsRange", "campfireTweaks.effectsRange"))
            .processors(registry -> {
                SpecialProcessors.collect(registry);
                DefaultProcessors.collect(registry);
                FeatureManager.runLegacy(registry);
            })
            .build();
    private static final AndromedaConfig CONFIG = MANAGER.getConfig();

    public static <T> T get(String feature) throws NoSuchFieldException {
        return MANAGER.get(feature);
    }

    public static Field set(String feature, Object value) throws NoSuchFieldException {
        MANAGER.set(feature, value);
        return MANAGER.getField(feature);
    }

    public static AndromedaConfig get() {
        return CONFIG;
    }

    public static AndromedaConfig getDefault() {
        return MANAGER.getDefaultConfig();
    }

    public static ConfigManager<AndromedaConfig> getManager() {
        return MANAGER;
    }

    public static void run(ThrowingRunnable runnable, String... features) {
        try {
            runnable.run();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(features)), e);
            FeatureManager.processUnknownException(e, features);
        }
    }

    public static <T> T run(Callable<T> callable, String... features) {
        try {
            return callable.call();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(features)), e);
            FeatureManager.processUnknownException(e, features);
            return null;
        }
    }
}
