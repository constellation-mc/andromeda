package me.melontini.andromeda.client.config;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.config.ConfigSerializer;
import me.melontini.andromeda.config.FeatureManager;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.annotations.config.FeatureEnvironment;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AutoConfigTransformers {

    public static void register() {
        AndromedaLog.info("Loading ClothConfig support!");

        AutoConfig.register(AndromedaConfig.class, (config, aClass) -> new ConfigSerializer());

        GuiRegistry registry = AutoConfig.getGuiRegistry(AndromedaConfig.class);

        registry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            list.forEach(gui -> {
                gui.setEditable(false);
                if (gui instanceof TooltipListEntry<?> tooltipGui) {
                    Set<String> fieldSet = FeatureManager.blameProcessors(field);
                    Set<Text> texts = new HashSet<>();
                    for (String string : fieldSet) {
                        if ("mod_json".equals(string)) {
                            texts.add(TextUtil.translatable("andromeda.config.tooltip.manager.mod_json", Arrays.toString(FeatureManager.blameMod(field))));
                        } else {
                            texts.add(TextUtil.translatable("andromeda.config.tooltip.manager." + string));
                        }
                    }
                    Text[] texts1 = texts.toArray(Text[]::new);
                    tooltipGui.setTooltipSupplier(() -> Optional.of(texts1));
                }
            });
            return list;
        }, FeatureManager::isModified);

        registry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            if (field.getType() == boolean.class || field.getType() == Boolean.class)
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
