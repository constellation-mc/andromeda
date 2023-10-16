package me.melontini.andromeda.config;

import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.dark_matter.api.base.util.classes.ThrowingRunnable;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;
import me.melontini.dark_matter.api.config.OptionManager;
import me.melontini.dark_matter.api.config.interfaces.TextEntry;
import me.melontini.dark_matter.api.config.serializers.gson.GsonSerializers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.Callable;

@SuppressWarnings("UnstableApiUsage")
public class Config {

    static final String DEFAULT_KEY = "andromeda.config.tooltip.manager.";

    private static final ConfigManager<AndromedaConfig> MANAGER = ConfigBuilder
            .create(AndromedaConfig.class, SharedConstants.MOD_CONTAINER, "andromeda")
            .attach(LambdaAccessors::attach)
            .constructor(AndromedaConfig::new)
            .serializer(manager -> GsonSerializers.create(manager, Fixups.addFixups()))
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
                AndromedaFeatureManager.runLegacy(registry);
            })
            .defaultReason(holder -> {
                if ("andromeda:custom_values".equals(holder.processor())) {
                    return TextEntry.translatable(DEFAULT_KEY + "mod_json", Arrays.toString(Config.getOptionManager().blameModJson(holder.field()).right().toArray()));
                }
                return TextEntry.translatable(DEFAULT_KEY + holder.processor().replace(":", "."));
            })
            .build();

    public static <T> T get(String feature) throws NoSuchFieldException {
        return MANAGER.get(feature);
    }

    public static Field set(String feature, Object value) throws NoSuchFieldException {
        MANAGER.set(feature, value);
        return MANAGER.getField(feature);
    }

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

    public static void run(ThrowingRunnable<Throwable> runnable, String... features) {
        try {
            runnable.run();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(features)), e);
            processUnknownException(e, features);
        }
    }

    public static <T> T run(Callable<T> callable, String... features) {
        try {
            return callable.call();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(features)), e);
            processUnknownException(e, features);
            return null;
        }
    }

    public static void processMixinError(String feature, String className) {
        SpecialProcessors.FAILED_MIXINS.put(feature, new SpecialProcessors.MixinErrorEntry(feature, false, className));
        MANAGER.save();
    }

    public static void processUnknownException(Throwable t, String... features) {
        for (String feature : features) {
            SpecialProcessors.UNKNOWN_EXCEPTIONS.put(feature, new SpecialProcessors.ExceptionEntry(feature, false, t));
        }
        MANAGER.save();
    }
}
