package me.melontini.andromeda.client.config;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.andromeda.util.annotations.config.FeatureEnvironment;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import me.melontini.dark_matter.api.config.OptionManager;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import me.shedaniel.autoconfig.gui.registry.ComposedGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class AutoConfigScreen {

    public static void register() {
        AndromedaLog.info("Loading ClothConfig support!");

        Holder.OURS.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            list.forEach(gui -> {
                gui.setEditable(false);
                if (gui instanceof TooltipListEntry<?> tooltipGui) {
                    Tuple<String, Set<OptionManager.ProcessorEntry<AndromedaConfig>>> tuple = Config.getOptionManager().blameProcessors(field);
                    Set<Text> texts = new HashSet<>();
                    for (OptionManager.ProcessorEntry<AndromedaConfig> processor : tuple.right()) {
                        Config.getOptionManager().getReason(processor.id(), tuple.left()).ifPresent(textEntry -> {
                            if (textEntry.isTranslatable()) {
                                texts.add(TextUtil.translatable(textEntry.get(), textEntry.args()));
                            } else {
                                texts.add(TextUtil.literal(textEntry.get()));
                            }
                        });
                    }
                    Text[] texts1 = texts.toArray(Text[]::new);
                    tooltipGui.setTooltipSupplier(() -> Optional.of(texts1));
                }
            });
            return list;
        }, field -> Config.getOptionManager().isModified(field));

        Holder.OURS.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            if (field.getType() == boolean.class || field.getType() == Boolean.class)
                list.forEach(gui -> gui.setRequiresRestart(true));
            return list;
        }, field -> Config.get().compatMode);

        Holder.OURS.registerAnnotationTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
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

    public static Optional<Screen> get(Screen screen) {
        return Support.get("cloth-config", () -> () -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(screen)
                    .setTitle(TextUtil.translatable("config.andromeda.title", SharedConstants.MOD_VERSION.split("-")[0]))
                    .setSavingRunnable(Config::save)
                    .setDefaultBackgroundTexture(Identifier.tryParse("minecraft:textures/block/amethyst_block.png"));

            Arrays.stream(AndromedaConfig.class.getDeclaredFields())
                    .collect(Collectors.groupingBy(
                            field -> getOrCreateCategoryForField(field, builder), LinkedHashMap::new, Collectors.toList()
                    )).forEach((category, fields) -> fields.forEach(field -> {
                        String opt = "text.autoconfig.andromeda.option." + field.getName();
                        Holder.COMPOSED.getAndTransform(opt, field, Config.get(), Config.getDefault(), Holder.COMPOSED).forEach(category::addEntry);
                    }));

            return builder.build();
        });
    }

    private static ConfigCategory getOrCreateCategoryForField(Field field, ConfigBuilder screenBuilder) {
        String name = Optional.ofNullable(field.getAnnotation(ConfigEntry.Category.class)).map(ConfigEntry.Category::value).orElse("default");

        Text key = TextUtil.translatable("text.autoconfig.andromeda.category.%s".formatted(name));
        return screenBuilder.getOrCreateCategory(key);
    }

    private static class Holder {
        static final GuiRegistry OURS = new GuiRegistry();
        static final GuiRegistry DEFAULT_REGISTRY = DefaultGuiTransformers.apply(DefaultGuiProviders.apply(new GuiRegistry()));
        static final GuiRegistryAccess COMPOSED = new ComposedGuiRegistryAccess(DEFAULT_REGISTRY, OURS);
    }
}
