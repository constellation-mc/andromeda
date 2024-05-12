package me.melontini.andromeda.common.client.config;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.CustomLog;
import lombok.With;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static me.melontini.andromeda.common.client.config.ClothTooltipTools.*;
import static me.melontini.andromeda.common.client.config.ModMenuIntegration.*;

@CustomLog
public class NewAutoConfigScreen {

    private static final Reference2ReferenceMap<Predicate<Class<?>>, Entry> PROVIDERS = new Reference2ReferenceOpenHashMap<>();
    static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();
    private static final Consumer<Object> DEFAULT_CONSUMER = object -> {};

    private static final Field saveCallback;

    static {
        saveCallback = Support.fallback("cloth-config", () -> {
            LOGGER.info("Loading ClothConfig support!");
            return Reflect.findField(AbstractConfigEntry.class, "saveCallback").orElseThrow();
        }, () -> {
            LOGGER.error("AutoConfigScreen class loaded without Cloth Config!");
            return null;
        });
        saveCallback.setAccessible(true);

        handleUnknownObject();

        NewProviders.init();
    }

    private static void handleUnknownObject() {
        PROVIDERS.defaultReturnValue(new Entry(transform((type, value, def, setter, i18n, context) -> {
            String classI13n;
            if (context.generic()) {
                String remainingI13n = i18n.substring(0, i18n.indexOf(".option") + ".option".length());
                classI13n = String.format("%s.%s", remainingI13n, type.getSimpleName());
            } else {
                classI13n = i18n;
            }

            Context context1 = context.withGeneric(false);
            List<CompletableFuture<AbstractConfigListEntry<?>>> builder = new ArrayList<>();
            List<Field> fields = new ArrayList<>(Arrays.asList(type.getFields()));
            fields.removeIf(field -> field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class));
            for (Field field : fields) {
                builder.add(CompletableFuture.supplyAsync(() -> {
                    String iI13n;
                    if (context.generic()) {
                        iI13n = String.format("%s.%s", classI13n, field.getName());
                    } else {
                        iI13n = String.format("%s.option.%s", classI13n, field.getName());
                    }
                    var e = PROVIDERS.entrySet().stream().filter(me -> me.getKey().test(field.getType())).findFirst().map(Map.Entry::getValue).orElse(PROVIDERS.defaultReturnValue());
                    var r = e.provider().getEntry(field.getType(), getField(field, value), getField(field, def),
                            object -> setField(field, value, object), iI13n, context1.withField(field));
                    if (r != null) {
                        r.setErrorSupplier(() -> e.errorSupplier().apply(r.getValue()));
                    }
                    return r;
                }));
            }

            List<AbstractConfigListEntry<?>> complete = Utilities.cast(builder.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList());
            return new MultiElementListEntry<>(TextUtil.translatable(classI13n), value, complete, false);
        }), (c) -> Exceptions.supply(() -> c.getConstructor().newInstance()), new Converters(Function.identity(), Function.identity()), object -> Optional.empty()));
    }

    public static <T> Builder<T> builder(Predicate<Class<?>> type, Provider<T> provider, Function<Class<?>, T> defSupplier) {
        return new Builder<>(type, provider, defSupplier);
    }

    public static <T> Builder<T> builder(Class<T> type, Provider<T> provider, Function<Class<?>, T> defSupplier) {
        return new Builder<>(type, provider, defSupplier);
    }

    public static Entry defaultProvider() {
        return PROVIDERS.defaultReturnValue();
    }

    public static Map<Predicate<Class<?>>, Entry> getProviders() {
        return Collections.unmodifiableMap(PROVIDERS);
    }

    private static final Splitter SPLITTER = Splitter.on("-");

    public static Screen get(Screen parent) {
        Map<Object, Set<SaveRunnable>> saveQueue = new LinkedHashMap<>();
        BiConsumer<Object, SaveRunnable> acceptor = (o, r) -> saveQueue.computeIfAbsent(o, object -> new LinkedHashSet<>()).add(r);

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(TextUtil.translatable("config.andromeda.title", Iterables.get(SPLITTER.split(CommonValues.version()), 0)))
                .setSavingRunnable(() -> powerSave(saveQueue))
                .setDefaultBackgroundTexture(Identifier.tryParse("minecraft:textures/block/amethyst_block.png"));

        Field field = Exceptions.supply(() -> MultiElementListEntry.class.getDeclaredField("entries"));
        field.setAccessible(true);

        var avProvider = PROVIDERS.entrySet().stream().filter(e -> e.getKey().test(BooleanIntermediary.class)).findFirst().orElseThrow().getValue();

        var handlers = ImmutableMap.of(ConfigState.MAIN, Andromeda.ROOT_HANDLER, ConfigState.GAME, Andromeda.GAME_HANDLER, ConfigState.CLIENT, AndromedaClient.HANDLER);
        var defProvider = PROVIDERS.defaultReturnValue().provider();

        boolean commander = FabricLoader.getInstance().isModLoaded("commander");

        ModuleManager.get().all().stream().map(Promise::get).forEach(module -> {
            var category = builder.getOrCreateCategory(TextUtil.translatable("config.andromeda.category.%s".formatted(module.meta().category())));

            String moduleText = "config.andromeda.%s".formatted(module.meta().dotted());
            var bootstrapConfig = ModuleManager.get().getConfig(module);

            var moduleCategory = ENTRY_BUILDER.startSubCategory(TextUtil.translatable(moduleText));

            handlers.forEach((state, handler) -> {
                var definition = module.getConfigDefinition(state);
                if (definition == null) return;

                var stateKey = "config.andromeda.state.%s".formatted(state.name().toLowerCase(Locale.ROOT));
                var stateCategory = ENTRY_BUILDER.startSubCategory(TextUtil.translatable(stateKey));

                var config = handler.get(definition);
                var defaultConfig = handler.getDefault(definition);

                Context context = new Context(false, () -> acceptor.accept(module, new SaveRunnable(definition, () -> handler.save(module))), null, module);
                if ((Experiments.get().scopedConfigs || commander) && Module.GameConfig.class.isAssignableFrom(definition.supplier().get())) {
                    var availableKey = "config.andromeda.option.available";

                    var available = avProvider.provider().getEntry(BooleanIntermediary.class,
                            ((Module.GameConfig) config).available, ((Module.GameConfig) defaultConfig).available,
                            object -> ((Module.GameConfig) config).available = (BooleanIntermediary) object,
                            availableKey, context
                    );
                    available.setErrorSupplier(() -> avProvider.errorSupplier().apply(available.getValue()));

                    stateCategory.add(available);
                }

                var e = defProvider.getEntry(config.getClass(),
                        config, defaultConfig, DEFAULT_CONSUMER,
                        moduleText, context);
                if (!(e instanceof MultiElementListEntry<?> listEntry))
                    throw new IllegalStateException(config.getClass().getName());
                List<AbstractConfigListEntry<?>> entries = getField(field, listEntry);
                stateCategory.addAll(entries);
                if (!stateCategory.isEmpty()) moduleCategory.add(stateCategory.build());
            });

            if (moduleCategory.isEmpty()) {
                var r = ENTRY_BUILDER.startBooleanToggle(TextUtil.translatable(moduleText), bootstrapConfig.enabled)
                        .setDefaultValue(() -> false)
                        .setSaveConsumer(b -> bootstrapConfig.enabled = b)
                        .requireRestart().build();
                category.addEntry(wrapSaveCallback(standardForModule(r, module, "enabled"), () -> acceptor.accept(module, new SaveRunnable(ModuleManager.get(), () -> ModuleManager.get().saveBootstrap(module)))));
                return;
            }

            AbstractConfigListEntry<?> enabled = ENTRY_BUILDER.startBooleanToggle(TextUtil.translatable("config.andromeda.option.enabled"), bootstrapConfig.enabled)
                    .setDefaultValue(() -> false)
                    .setSaveConsumer(b -> bootstrapConfig.enabled = b)
                    .requireRestart().build();
            moduleCategory.add(0, wrapSaveCallback(enabled, () -> acceptor.accept(module, new SaveRunnable(ModuleManager.get(), () -> ModuleManager.get().saveBootstrap(module)))));
            category.addEntry(standardForModule(moduleCategory.build(), module, null));
        });

        ConfigCategory misc = builder.getOrCreateCategory(TextUtil.translatable("config.andromeda.category.misc"));

        var e = defProvider.getEntry(AndromedaConfig.Config.class,
                AndromedaConfig.get(), AndromedaConfig.getDefault(), DEFAULT_CONSUMER, "config.andromeda.base",
                new Context(false, () -> acceptor.accept(AndromedaConfig.class, new SaveRunnable(null, AndromedaConfig::save)), null, null));
        if (!(e instanceof MultiElementListEntry<?> listEntry))
            throw new IllegalStateException(AndromedaConfig.Config.class.getName());
        List<AbstractConfigListEntry<?>> entries = getField(field, listEntry);
        entries.forEach(misc::addEntry);

        var screen = builder.build();
        ScreenEvents.AFTER_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
            if (screen != screen1) return;

            var wiki = getWikiButton(client, screen);
            addDrawableChild(screen, wiki);

            var lab = new TexturedButtonWidget(screen.width - 62, 13, 20, 20, 0, 0, 20, LAB_BUTTON_TEXTURE, 32, 64, button -> client.setScreen(getLabScreen(screen1)));
            lab.setTooltip(Tooltip.of(TextUtil.translatable("config.andromeda.button.lab.tooltip")));
            addDrawableChild(screen, lab);
        });
        return screen;
    }

    private static Screen getLabScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(TextUtil.translatable("config.andromeda.lab.title"))
                .setSavingRunnable(Experiments::save)
                .setDefaultBackgroundTexture(Identifier.tryParse("minecraft:textures/block/amethyst_block.png"));

        ConfigCategory main = builder.getOrCreateCategory(TextUtil.translatable("config.andromeda.category.main"));
        Field field = Exceptions.supply(() -> MultiElementListEntry.class.getDeclaredField("entries"));
        field.setAccessible(true);

        var e = PROVIDERS.defaultReturnValue().provider().getEntry(
                Experiments.Config.class,
                Experiments.get(), Experiments.getDefault(), DEFAULT_CONSUMER, "config.andromeda.lab",
                new Context(false, () -> {
                }, null, null));
        if (!(e instanceof MultiElementListEntry<?> listEntry))
            throw new IllegalStateException(AndromedaConfig.Config.class.getName());
        List<AbstractConfigListEntry<?>> entries = getField(field, listEntry);
        entries.forEach(main::addEntry);

        return builder.build();
    }

    @NotNull private static TexturedButtonWidget getWikiButton(MinecraftClient client, Screen screen) {
        var wiki = new TexturedButtonWidget(screen.width - 40, 13, 20, 20, 0, 0, 20, WIKI_BUTTON_TEXTURE, 32, 64, button -> {
            if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
                Debug.load();
                ScreenParticleHelper.addScreenParticles(ParticleTypes.ANGRY_VILLAGER, screen.width - 30, 23, 0.5, 0.5, 0.5, 1);
                LOGGER.info("Reloaded Debug Keys!");
            } else {
                screen.handleTextClick(WIKI_LINK);
            }
        });
        wiki.setTooltip(Tooltip.of(TextUtil.translatable("config.andromeda.button.wiki")));
        return wiki;
    }

    private static <T extends AbstractConfigListEntry<?>> T standardForModule(T e, Module module, String field) {
        if (field == null || checkOptionManager(e, module, field)) {
            setModuleTooltip(e, module);
            appendEnvInfo(e, module.meta().environment());
        }
        appendOrigin(e, module);
        appendDeprecationInfo(e, module);
        return wrapTooltip(e);
    }

    private static <T extends AbstractConfigEntry<?>> T wrapSaveCallback(T e, Runnable saveFunc) {
        Consumer<Object> original = (Consumer<Object>) Exceptions.supply(() -> saveCallback.get(e));
        if (original == null) return e;
        Exceptions.run(() -> saveCallback.set(e, (Consumer<Object>) o -> {
            if (e.isEdited()) {
                original.accept(o);
                saveFunc.run();
            }
        }));
        return e;
    }

    private static void powerSave(Map<Object, Set<SaveRunnable>> saveQueue) {
        saveQueue.values().forEach(set -> CompletableFuture.runAsync(() -> set.forEach(r -> r.function().run())));
        saveQueue.clear();
    }

    private static void setField(Field field, Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T getField(Field field, Object object) {
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public interface Provider<T> {
        AbstractConfigListEntry<?> getEntry(Class<?> type, T value, @Nullable T def, Consumer<T> setter, String i18n, Context context);
    }

    public record Converters(Function<Object, Object> fromBase, Function<Object, Object> toBase) {
    }

    public record Entry(Provider<Object> provider, Function<Class<?>, Object> def, Converters converters,
                        Function<Object, Optional<Text>> errorSupplier) {
    }

    private static <T> Provider<T> transform(Provider<T> provider) {
        return (type, value, def, setter, i18n, context) -> {
            var r = provider.getEntry(type, value, def, setter, i18n, context);
            if (context.field() != null && context.field().isAnnotationPresent(ConfigEntry.Gui.RequiresRestart.class))
                r.setRequiresRestart(true);
            if ((context.field() == null || context.module() == null) || checkOptionManager(r, context.module(), context.field().getName())) {
                if (!context.generic()) setOptionTooltip(r, i18n + ".@Tooltip");
                if (context.field() != null) appendEnvInfo(r, context.field());
            }
            return wrapSaveCallback(wrapTooltip(r), context.saver());
        };
    }

    public static class Builder<T> {
        private final Predicate<Class<?>> predicate;
        private final Provider<T> provider;
        private final Function<Class<?>, T> defSupplier;
        private Converters converters;
        private Function<?, Optional<Text>> errorSupplier;

        public Builder(Class<T> type, Provider<T> provider, Function<Class<?>, T> defSupplier) {
            this.predicate = c -> c == type;
            this.provider = transform(provider);
            this.defSupplier = defSupplier;
        }

        private Builder(Predicate<Class<?>> predicate, Provider<T> provider, Function<Class<?>, T> defSupplier) {
            this.predicate = predicate;
            this.provider = transform(provider);
            this.defSupplier = defSupplier;
        }

        public <B> Builder<T> converter(Function<B, T> fromBase, Function<T, B> toBase) {
            this.converters = new Converters((Function<Object, Object>) fromBase, (Function<Object, Object>) toBase);
            return this;
        }

        public <B> Builder<T> errorSupplier(Function<B, Optional<Text>> errorSupplier) {
            this.errorSupplier = errorSupplier;
            return this;
        }

        public void build() {
            var entry = new Entry(
                    (Provider<Object>) provider,
                    (Function<Class<?>, Object>) defSupplier,
                    converters == null ? new Converters(Function.identity(), Function.identity()) : converters,
                    errorSupplier == null ? object -> Optional.empty() : (Function<Object, Optional<Text>>) errorSupplier);

            PROVIDERS.put(predicate, entry);
        }
    }

    @With
    public record Context(boolean generic, Runnable saver, Field field, Module module) {
    }

    private record SaveRunnable(Object tag, Runnable function) {

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            SaveRunnable that = (SaveRunnable) object;
            return Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(tag);
        }
    }
}
