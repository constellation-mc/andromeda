package dev.zenfyr.andromeda.common.client.config;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.common.config.handler.MultiConfigHandler;
import dev.zenfyr.andromeda.common.mixin.MultiElementListEntryAccessor;
import dev.zenfyr.andromeda.util.AndromedaConstants;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.With;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoConfigScreen {

  public static final Component RESET_BUTTON_KEY =
      Component.translatable("text.cloth-config.reset_value");

  private final ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
  private final IdentityHashMap<Class<?>, Entry<?>> providers = new IdentityHashMap<>();
  private final Map<@NotNull MultiConfigHandler, @NotNull String> handlers;

  private final Entry<Object> objectEntry;
  private final Entry<Enum<?>> enumEntry;

  public AutoConfigScreen() {
    EntryProviders.init(this);
    this.handlers =
        Map.of(Andromeda.MAIN, "main", Andromeda.GAME, "game", AndromedaClient.CLIENT, "client");

    this.objectEntry = (Entry<Object>) this.providers.get(Object.class);
    this.enumEntry = (Entry<Enum<?>>) this.providers.get(Enum.class);
  }

  public ConfigEntryBuilder entryBuilder() {
    return this.entryBuilder;
  }

  public <T> void register(
      Class<T> type, EntryFunction<T> entryFunction, Function<Class<?>, T> def) {
    providers.put(
        type,
        new Entry<>(
            context -> {
              var entry = entryFunction.getEntry(context);

              if (context.field() != null) {
                if (context.field().isAnnotationPresent(ConfigEntry.Gui.RequiresRestart.class))
                  entry.setRequiresRestart(true);
              }

              if (context.module() != null) {
                ClothTooltipUtil.setEntryTooltip(entry, context.i18n() + ".@Tooltip");
              }

              return ClothTooltipUtil.wrapTooltip(entry);
            },
            def));
  }

  public <T> Entry<T> getEntry(Class<T> type) {
    var e = (Entry<T>) providers.get(type);
    if (e != null) return e;

    if (type.isEnum() || Enum.class.isAssignableFrom(type)) {
      return (Entry<T>) this.enumEntry;
    }

    return (Entry<T>) this.objectEntry;
  }

  public Screen getScreen(Screen parent) {
    var manager = ModuleManager.get();

    Map<Object, Runnable> saveQueue = new IdentityHashMap<>();

    ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(TextUtil.translatable("config.andromeda.title", AndromedaConstants.VERSION))
        .setSavingRunnable(() -> {
          saveQueue.values().forEach(Runnable::run);
          saveQueue.clear();
        })
        .setDefaultBackgroundTexture(
            Identifier.tryBuild("minecraft", "textures/block/amethyst_block.png"));

    var bootstrapHandler = manager.configHandler();

    for (Module module :
        manager.all().stream().sorted(Comparator.comparing(ModuleHelper::id)).toList()) {
      var category = builder.getOrCreateCategory(TextUtil.translatable(
          "config.andromeda.category.%s".formatted(module.meta().category())));

      String moduleText = "config.andromeda.%s".formatted(ModuleHelper.dotted(module.meta()));
      var moduleCategory = this.entryBuilder().startSubCategory(TextUtil.translatable(moduleText));

      this.handlers.forEach((handler, state) -> {
        var definition = handler.getDefinition(module);
        if (definition == null) return;

        var stateKey = "config.andromeda.state.%s".formatted(state);
        var stateCategory = this.entryBuilder().startSubCategory(TextUtil.translatable(stateKey));

        var config = handler.get(definition);
        var defConfig = handler.getDefault(definition);

        var e = this.objectEntry
            .function()
            .getEntry(makeRootCtx(
                config,
                defConfig,
                o -> saveQueue.put(definition, () -> handler.save(module)),
                moduleText,
                module));
        stateCategory.addAll(((MultiElementListEntryAccessor) e).pulsar$entries());
        if (!stateCategory.isEmpty()) moduleCategory.add(stateCategory.build());
      });

      var rootI18n = moduleCategory.isEmpty()
          ? TextUtil.translatable(moduleText)
          : TextUtil.translatable("config.andromeda.option.enabled");
      var bootstrapConfig = bootstrapHandler.get(module);

      var rootToggle = this.entryBuilder()
          .startBooleanToggle(rootI18n, bootstrapConfig.enabled)
          .setDefaultValue(() -> false)
          .setSaveConsumer(b -> {
            bootstrapConfig.enabled = b;
            saveQueue.put(bootstrapConfig, () -> bootstrapHandler.save(module));
          })
          .requireRestart()
          .build();
      if (moduleCategory.isEmpty()) {
        category.addEntry(ClothTooltipUtil.standardForModule(rootToggle, module, "enabled"));
      } else {
        moduleCategory.add(0, rootToggle);
        category.addEntry(ClothTooltipUtil.standardForModule(moduleCategory.build(), module, null));
      }
    }

    return builder.build();
  }

  private static <T> EntryContext<T> makeRootCtx(
      T value, T def, Consumer<T> consumer, String i18n, Module module) {
    return new EntryContext<>(value.getClass(), value, def, consumer, i18n, null, module);
  }

  @With
  public record EntryContext<T>(
      Class<?> type,
      T value,
      @Nullable T def,
      Consumer<T> consumer,
      String i18n,
      Field field,
      Module module) {}

  public interface EntryFunction<T> {
    AbstractConfigListEntry<?> getEntry(EntryContext<T> context);
  }

  public record Entry<T>(EntryFunction<T> function, Function<Class<?>, T> def) {}
}
