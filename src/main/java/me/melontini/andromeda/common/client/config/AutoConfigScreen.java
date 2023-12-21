package me.melontini.andromeda.common.client.config;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.Origin;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.config.AndromedaConfig;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.common.annotations.GameRule;
import me.melontini.andromeda.common.client.OrderedTextUtil;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class AutoConfigScreen {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Field> saveCallback = Reflect.findField(AbstractConfigEntry.class, "saveCallback");
    private static final ThreadLocal<Set<Runnable>> saveQueue = ThreadLocal.withInitial(HashSet::new);

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
                List<Field> fields = MakeSure.notEmpty(Arrays.asList(ModuleManager.get().getConfigClass(module.getClass()).getFields()));
                fields.sort(Comparator.comparingInt(value -> !"enabled".equals(value.getName()) ? 1 : 0));

                var category = getOrCreateCategoryForField(module, builder);
                String moduleText = "config.andromeda.%s".formatted(module.meta().dotted());

                if (fields.size() <= 2) {
                    registry.getAndTransform(moduleText, fields.get(0), module.config(), module.defaultConfig(), registry)
                            .forEach(e -> {
                                if (checkOptionManager(e, module, fields.get(0))) {
                                    setModuleTooltip(e, module);
                                    appendEnvInfo(e, module.meta().environment());
                                }
                                appendOrigin(e, module);
                                wrapTooltip(e);
                                wrapSaveCallback(e, module::save);
                                category.addEntry(e);
                            });
                } else {
                    List<AbstractConfigListEntry<?>> list = new ArrayList<>();
                    fields.forEach((field) -> {
                        String opt = "enabled".equals(field.getName()) ? "config.andromeda.option.enabled" : "config.andromeda.%s.option.%s".formatted(module.meta().dotted(), field.getName());
                        registry.getAndTransform(opt, field, module.config(), module.defaultConfig(), registry).forEach(e -> {
                            if (checkOptionManager(e, module, field)) {
                                appendGameRuleInfo(e, field);
                                appendEnvInfo(e, field);
                            }
                            wrapTooltip(e);
                            wrapSaveCallback(e, module::save);
                            list.add(e);
                        });
                    });
                    var e = eb.startSubCategory(TextUtil.translatable("config.andromeda.%s".formatted(module.meta().dotted())), Utilities.cast(list));
                    var built = e.build();
                    setModuleTooltip(built, module);
                    appendOrigin(built, module);
                    appendEnvInfo(built, module.meta().environment());
                    wrapTooltip(built);
                    category.addEntry(built);
                }
            });

            ConfigCategory misc = builder.getOrCreateCategory(TextUtil.translatable("config.andromeda.category.misc"));
            Arrays.stream(AndromedaConfig.class.getFields()).forEach((field) -> {
                String opt = "config.andromeda.base.option." + field.getName();
                registry.getAndTransform(opt, field, Config.get(), Config.getDefault(), registry).forEach(e -> {
                    //if (checkOptionManager(e, m, field)) {
                        appendEnvInfo(e, field);
                    //}
                    wrapTooltip(e);
                    wrapSaveCallback(e, Config::save);
                    misc.addEntry(e);
                });
            });

            return builder.build();
        });
    }

    private static void wrapSaveCallback(AbstractConfigEntry<?> e, Runnable saveFunc) {
        if (saveCallback.isPresent()) {
            saveCallback.get().setAccessible(true);
            Consumer<Object> original = (Consumer<Object>) Utilities.supplyUnchecked(() -> saveCallback.get().get(e));
            if (original != null) {
                Utilities.runUnchecked(() -> saveCallback.get().set(e, (Consumer<Object>) o -> {
                    if (e.isEdited()) {
                        original.accept(o);
                        saveQueue.get().add(saveFunc);
                    }
                }));
            }
        }
    }

    private static boolean checkOptionManager(AbstractConfigListEntry<?> e, Module<?> module, Field field) {
        var opt = FeatureBlockade.get().explain(module, field.getName());
        if (opt.isPresent()) {
            e.setEditable(false);
            if (e instanceof TooltipListEntry<?> t) {
                Optional<Text[]> optional = Optional.of(opt.get().stream().map(text -> {
                    if (text instanceof MutableText mt) return mt.formatted(Formatting.RED);
                    return text.copy().formatted(Formatting.RED);
                }).toArray(Text[]::new));
                t.setTooltipSupplier(() -> optional);
            }
            return false;
        }
        return true;
    }

    private static void appendGameRuleInfo(AbstractConfigListEntry<?> e, Field f) {
        if (f.isAnnotationPresent(GameRule.class)) {
            if (e instanceof TooltipListEntry<?> t) {
                appendText(t, TextUtil.translatable("andromeda.config.tooltip.game_rule").formatted(Formatting.YELLOW));
            }
        }
    }

    private static void appendOrigin(AbstractConfigListEntry<?> e, Module<?> module) {
        if (module.getClass().isAnnotationPresent(Origin.class)) {
            Origin origin = module.getClass().getAnnotation(Origin.class);
            if (e instanceof TooltipListEntry<?> t) {
                appendText(t, TextUtil.translatable("andromeda.config.tooltip.origin", origin.mod(), origin.author()).formatted(Formatting.DARK_AQUA));
            }
        }
    }

    private static void appendEnvInfo(AbstractConfigListEntry<?> e, Environment env) {
        if (e instanceof TooltipListEntry<?> t) {
            Text text = TextUtil.translatable("andromeda.config.tooltip.environment." + env.toString().toLowerCase()).formatted(Formatting.YELLOW);
            appendText(t, text);
        }
    }

    private static void appendEnvInfo(AbstractConfigListEntry<?> e, Field f) {
        if (f.isAnnotationPresent(SpecialEnvironment.class) && e instanceof TooltipListEntry<?> t) {
            SpecialEnvironment env = f.getAnnotation(SpecialEnvironment.class);
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

    private static void powerSave() {
        Config.save();
        if (saveCallback.isPresent()) {
            saveQueue.get().forEach(Runnable::run);
            saveQueue.get().clear();
        } else {
            ModuleManager.get().all().forEach(Module::save);
        }
    }

    private static ConfigCategory getOrCreateCategoryForField(Module<?> info, ConfigBuilder screenBuilder) {
        Text key = TextUtil.translatable("config.andromeda.category.%s".formatted(info.meta().category()));
        return screenBuilder.getOrCreateCategory(key);
    }
}
