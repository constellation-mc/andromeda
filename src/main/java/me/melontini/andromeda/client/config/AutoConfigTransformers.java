package me.melontini.andromeda.client.config;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.config.ConfigSerializer;
import me.melontini.andromeda.config.FeatureManager;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.annotations.config.FeatureEnvironment;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Optional;

public class AutoConfigTransformers {

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) return;
        AndromedaLog.info("Loading ClothConfig support!");

        AutoConfig.register(AndromedaConfig.class, (config, aClass) -> new ConfigSerializer());

        AutoConfig.getConfigHolder(AndromedaConfig.class).setConfig(Config.get());//ugh

        AutoConfig.getConfigHolder(AndromedaConfig.class).registerSaveListener((configHolder, config) -> {
            FeatureManager.processFeatures(Utilities.isDev());
            return ActionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfig.getGuiRegistry(AndromedaConfig.class);

        registry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) ->
                list.stream().peek(gui -> {
                    gui.setRequirement(() -> !FeatureManager.isModified(field));
                    if (gui instanceof TooltipListEntry<?> tooltipGui) {
                        if ("mod_json".equals(FeatureManager.blameProcessor(field))) {
                            Text[] manager = new Text[]{TextUtil.translatable("andromeda.config.tooltip.manager.mod_json", Arrays.toString(FeatureManager.blameMod(field)))};
                            tooltipGui.setTooltipSupplier(() -> Optional.of(manager));
                        } else {
                            Text[] manager = new Text[]{TextUtil.translatable("andromeda.config.tooltip.manager." + FeatureManager.blameProcessor(field))};
                            tooltipGui.setTooltipSupplier(() -> Optional.of(manager));
                        }
                    }
                }).toList(), FeatureManager::isModified);

        registry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            list.forEach(gui -> gui.setRequiresRestart(true));
            return list;
        }, field -> Config.get().compatMode);

        registry.registerAnnotationTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            list.forEach(gui -> {
                if (gui instanceof TooltipListEntry<?> tooltipGui) {
                    FeatureEnvironment environment = field.getAnnotation(FeatureEnvironment.class);

                    if (tooltipGui.getTooltipSupplier() != null) {
                        Optional<Text[]> optional = tooltipGui.getTooltipSupplier().get();
                        Text[] text = optional.map(texts -> ArrayUtils.add(texts, TextUtil.translatable("andromeda.config.tooltip.environment." + environment.value().toString().toLowerCase()))).orElseGet(() -> new Text[]{TextUtil.translatable("andromeda.config.tooltip.environment." + environment.value().toString().toLowerCase())});
                        tooltipGui.setTooltipSupplier(() -> Optional.of(text));
                    } else {
                        Text[] text = new Text[]{TextUtil.translatable("andromeda.config.tooltip.environment." + environment.value().toString().toLowerCase())};
                        tooltipGui.setTooltipSupplier(() -> Optional.of(text));
                    }
                }
            });
            return list;
        }, FeatureEnvironment.class);

    }

}
