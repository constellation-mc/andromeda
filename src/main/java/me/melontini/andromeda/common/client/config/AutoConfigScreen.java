package me.melontini.andromeda.common.client.config;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.AndromedaConfig;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.OptionManager;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.dark_matter.impl.config.FieldOption;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import me.shedaniel.autoconfig.gui.registry.ComposedGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class AutoConfigScreen {

    private static final ThreadLocal<Module<?>> CONTEXT = new ThreadLocal<>();

    public static void register() {
        AndromedaLog.info("Loading ClothConfig support!");

        Holder.OURS.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            list.forEach(gui -> {
                gui.setEditable(false);
                if (gui instanceof TooltipListEntry<?> tooltipGui) {
                    var tuple = CONTEXT.get().manager().getOptionManager().blameProcessors(new FieldOption(field));
                    Set<Text> texts = new HashSet<>();
                    for (OptionManager.ProcessorEntry<?> processor : tuple.right()) {
                        CONTEXT.get().manager().getOptionManager().getReason(processor.id(), tuple.left()).ifPresent(textEntry -> {
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
        }, field -> CONTEXT.get() != null && CONTEXT.get().manager()
                .getOptionManager().isModified(new FieldOption(field)));

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
                    .setTitle(TextUtil.translatable("config.andromeda.title", CommonValues.version().split("-")[0]))
                    .setSavingRunnable(AutoConfigScreen::powerSave)
                    .setDefaultBackgroundTexture(Identifier.tryParse("minecraft:textures/block/amethyst_block.png"));

            var eb = builder.entryBuilder();
            ModuleManager.get().all().forEach(module -> {
                try {
                    CONTEXT.set(module);

                    List<Field> fields = Arrays.asList(module.configClass().getFields());
                    fields.sort(Comparator.comparingInt(value -> !"enabled".equals(value.getName()) ? 1 : 0));

                    Supplier<Optional<Text[]>> supplier = Optional::empty;
                    if (module.getClass().isAnnotationPresent(ModuleTooltip.class)) {
                        ModuleTooltip tooltip = module.getClass().getAnnotation(ModuleTooltip.class);
                        if (tooltip.value() == 1) {
                            Text[] text = new Text[]{TextUtil.translatable("config.andromeda.%s.@Tooltip".formatted(module.meta().dotted()))};
                            supplier = () -> Optional.of(text);
                        } else if (tooltip.value() > 1) {
                            Text[] text = IntStream.range(0, tooltip.value()).boxed()
                                    .map(i -> "config.andromeda.%s.@Tooltip[%s]".formatted(module.meta().dotted(), i))
                                    .map(TextUtil::translatable).toArray(Text[]::new);
                            supplier = () -> Optional.of(text);
                        }
                    }

                    if (fields.size() == 1) {
                        Field f = fields.get(0);
                        if (!"enabled".equals(f.getName())) throw new IllegalStateException();

                        var e = eb.startBooleanToggle(TextUtil.translatable("config.andromeda.%s".formatted(module.meta().dotted())), Utilities.supplyUnchecked(() -> f.getBoolean(module.config())));
                        e.setTooltipSupplier(supplier);
                        e.setDefaultValue(() -> Utilities.supplyUnchecked(() -> f.getBoolean(module.manager().getDefaultConfig())));
                        e.setSaveConsumer(b -> Utilities.runUnchecked(() -> f.setBoolean(module.config(), b)));
                        getOrCreateCategoryForField(module, builder).addEntry(e.build());
                    } else {
                        List<AbstractConfigListEntry<?>> list = new ArrayList<>();
                        fields.forEach((field) -> {
                            String opt = "enabled".equals(field.getName()) ? "config.andromeda.option.enabled" : "config.andromeda.%s.option.%s".formatted(module.meta().dotted(), field.getName());
                            Holder.COMPOSED.getAndTransform(opt, field, module.config(), module.manager().getDefaultConfig(), Holder.COMPOSED).forEach(list::add);
                        });
                        var e = eb.startSubCategory(TextUtil.translatable("config.andromeda.%s".formatted(module.meta().dotted())), Utilities.cast(list));
                        e.setTooltipSupplier(supplier);
                        getOrCreateCategoryForField(module, builder).addEntry(e.build());
                    }
                } finally {
                    CONTEXT.remove();
                }
            });

            ConfigCategory misc = builder.getOrCreateCategory(TextUtil.translatable("config.andromeda.category.misc"));
            Arrays.stream(AndromedaConfig.class.getFields()).forEach((field) -> {
                String opt = "config.andromeda.base.option." + field.getName();
                Holder.COMPOSED.getAndTransform(opt, field, Config.get(), Config.getDefault(), Holder.COMPOSED).forEach(misc::addEntry);
            });
            return builder.build();
        });
    }

    //TODO
    private static void powerSave() {
        Config.save();
        ModuleManager.get().all().forEach(module -> module.manager().save());
    }

    private static ConfigCategory getOrCreateCategoryForField(Module<?> info, ConfigBuilder screenBuilder) {
        Text key = TextUtil.translatable("config.andromeda.category.%s".formatted(info.meta().category()));
        return screenBuilder.getOrCreateCategory(key);
    }

    private static class Holder {
        static final GuiRegistry OURS = new GuiRegistry();
        static final GuiRegistry DEFAULT_REGISTRY = DefaultGuiTransformers.apply(DefaultGuiProviders.apply(new GuiRegistry()));
        static final GuiRegistryAccess COMPOSED = new ComposedGuiRegistryAccess(DEFAULT_REGISTRY, OURS);
    }
}
