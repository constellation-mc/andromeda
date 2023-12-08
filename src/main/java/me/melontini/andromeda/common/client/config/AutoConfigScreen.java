package me.melontini.andromeda.common.client.config;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.common.client.OrderedTextUtil;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.interfaces.Option;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class AutoConfigScreen {

    public static void register() {
        AndromedaLog.info("Loading ClothConfig support!");
    }

    public static Optional<Screen> get(Screen screen) {
        return Support.get("cloth-config", () -> () -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(screen)
                    .setTitle(TextUtil.translatable("config.andromeda.title", CommonValues.version().split("-")[0]))
                    .setSavingRunnable(AutoConfigScreen::powerSave)
                    .setDefaultBackgroundTexture(Identifier.tryParse("minecraft:textures/block/amethyst_block.png"));

            var eb = builder.entryBuilder();

            GuiRegistry registry = DefaultGuiTransformers.apply(DefaultGuiProviders.apply(new GuiRegistry()));

            ModuleManager.get().all().forEach(module -> {
                List<Field> fields = MakeSure.notEmpty(Arrays.asList(module.configClass().getFields()));
                fields.sort(Comparator.comparingInt(value -> !"enabled".equals(value.getName()) ? 1 : 0));

                var category = getOrCreateCategoryForField(module, builder);
                String moduleText = "config.andromeda.%s".formatted(module.meta().dotted());

                if (fields.size() == 1) {
                    registry.getAndTransform(moduleText, fields.get(0), module.manager().getConfig(), module.manager().getDefaultConfig(), registry)
                            .forEach(e -> {
                                if (checkOptionManager(e, module, fields.get(0))) {
                                    setModuleTooltip(e, module);
                                    wrapTooltip(e);
                                    appendEnvInfo(e, module);
                                }
                                category.addEntry(e);
                            });
                } else {
                    List<AbstractConfigListEntry<?>> list = new ArrayList<>();
                    fields.forEach((field) -> {
                        String opt = "enabled".equals(field.getName()) ? "config.andromeda.option.enabled" : "config.andromeda.%s.option.%s".formatted(module.meta().dotted(), field.getName());
                        registry.getAndTransform(opt, field, module.config(), module.manager().getDefaultConfig(), registry).forEach(e -> {
                            if (checkOptionManager(e, module, field)) {
                                wrapTooltip(e);
                                appendEnvInfo(e, field);
                            }
                            list.add(e);
                        });
                    });
                    var e = eb.startSubCategory(TextUtil.translatable("config.andromeda.%s".formatted(module.meta().dotted())), Utilities.cast(list));
                    var built = e.build();
                    setModuleTooltip(built, module);
                    wrapTooltip(built);
                    appendEnvInfo(built, module);
                    category.addEntry(built);
                }
            });

            return builder.build();
        });
    }

    private static boolean checkOptionManager(AbstractConfigListEntry<?> e, Module<?> module, Field field) {
        var opManager = module.manager().getOptionManager();
        Option opt = Option.ofField(field);
        if (opManager.isModified(opt)) {
            e.setEditable(false);

            if (e instanceof TooltipListEntry<?> t) {
                var tuple = opManager.blameProcessors(opt);
                Set<Text> texts = new LinkedHashSet<>();
                tuple.right().forEach(entry -> opManager.getReason(entry.id(), tuple.left()).ifPresent(textEntry -> {
                    if (textEntry.isTranslatable()) {
                        texts.add(TextUtil.translatable(textEntry.get(), textEntry.args()).formatted(Formatting.RED));
                    } else {
                        texts.add(TextUtil.literal(textEntry.get()).formatted(Formatting.RED));
                    }
                }));

                Optional<Text[]> optional = Optional.of(texts.toArray(Text[]::new));
                t.setTooltipSupplier(() -> optional);
            }
            return false;
        }
        return true;
    }

    private static void appendEnvInfo(AbstractConfigListEntry<?> e, Module<?> module) {
        if (e instanceof TooltipListEntry<?> t) {
            Text text = TextUtil.translatable("andromeda.config.tooltip.environment." + module.meta().environment().toString().toLowerCase()).formatted(Formatting.YELLOW);
            appendText(t, text);
        }
    }

    private static void appendEnvInfo(AbstractConfigListEntry<?> e, Field f) {
        if (f.isAnnotationPresent(FeatureEnvironment.class) && e instanceof TooltipListEntry<?> t) {
            FeatureEnvironment env = f.getAnnotation(FeatureEnvironment.class);
            Text text = TextUtil.translatable("andromeda.config.tooltip.environment." + env.value().toString().toLowerCase()).formatted(Formatting.YELLOW);
            appendText(t, text);
        }
    }

    private static void appendText(TooltipListEntry<?> t, Text text) {
        var supplier = t.getTooltipSupplier();
        Optional<Text[]> tooltip;
        if (supplier != null) {
            tooltip = Optional.of(supplier.get().map(texts -> ArrayUtils.add(texts, text))
                    .orElseGet(() -> new Text[]{text}));
        } else {
            tooltip = Optional.of(new Text[]{text});
        }
        t.setTooltipSupplier(() -> tooltip);
    }

    private static void setModuleTooltip(AbstractConfigListEntry<?> e, Module<?> module) {
        if (module.getClass().isAnnotationPresent(ModuleTooltip.class) && e instanceof TooltipListEntry<?> t) {
            var opt = Optional.of(new Text[]{TextUtil.translatable("config.andromeda.%s.@Tooltip".formatted(module.meta().dotted()))});
            t.setTooltipSupplier(() -> opt);
        }
    }

    private static void wrapTooltip(AbstractConfigListEntry<?> e) {
        if (e instanceof TooltipListEntry<?> t) {
            var supplier = t.getTooltipSupplier();
            if (supplier != null) {
                var opt = supplier.get().map(texts -> {
                    List<Text> wrapped = new ArrayList<>();
                    for (Text text : texts) {
                        wrapped.addAll(OrderedTextUtil.wrap(text, 250));
                    }
                    return wrapped.toArray(Text[]::new);
                });
                t.setTooltipSupplier(() -> opt);
            }
        }
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
}
