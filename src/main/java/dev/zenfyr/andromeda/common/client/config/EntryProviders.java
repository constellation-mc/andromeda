package dev.zenfyr.andromeda.common.client.config;

import dev.zenfyr.andromeda.common.util.TranslationKeyProvider;
import dev.zenfyr.pulsar.api.util.ExceptionUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import dev.zenfyr.pulsar.api.util.Utilities;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class EntryProviders {

  private static final Function<Enum<?>, Component> DEFAULT_NAME_PROVIDER = (t) -> {
    if (t instanceof TranslationKeyProvider provider) { // DIY API
      return provider
          .getTranslationKey()
          .map(TextUtil::translatable)
          .orElseGet(() -> TextUtil.literal(t.name()));
    }
    return TextUtil.literal(t.name());
  };

  static void init(AutoConfigScreen registry) {
    Set.of(int.class, Integer.class)
        .forEach(cl -> registry.register(
            cl,
            context -> registry
                .entryBuilder()
                .startIntField(i18n(context), context.value())
                .setDefaultValue(def(context))
                .setSaveConsumer(context.consumer())
                .build(),
            c -> 0));

    Set.of(boolean.class, Boolean.class)
        .forEach(cl -> registry.register(
            cl,
            context -> registry
                .entryBuilder()
                .startBooleanToggle(i18n(context), context.value())
                .setDefaultValue(def(context))
                .setSaveConsumer(context.consumer())
                .build(),
            c -> false));

    Set.of(float.class, Float.class)
        .forEach(cl -> registry.register(
            cl,
            context -> registry
                .entryBuilder()
                .startFloatField(i18n(context), context.value())
                .setDefaultValue(def(context))
                .setSaveConsumer(context.consumer())
                .build(),
            c -> 0f));

    Set.of(double.class, Double.class)
        .forEach(cl -> registry.register(
            cl,
            context -> registry
                .entryBuilder()
                .startDoubleField(i18n(context), context.value())
                .setDefaultValue(def(context))
                .setSaveConsumer(context.consumer())
                .build(),
            c -> 0d));

    registry.register(
        String.class,
        context -> registry
            .entryBuilder()
            .startStrField(i18n(context), context.value())
            .setDefaultValue(def(context))
            .setSaveConsumer(context.consumer())
            .build(),
        c -> "");

    registry.register(
        ResourceLocation.class,
        context -> new ResourceLocationListEntry(
            i18n(context),
            context.value(),
            AutoConfigScreen.RESET_BUTTON_KEY,
            def(context),
            context.consumer(),
            null,
            false),
        c -> new ResourceLocation(""));

    registry.register(
        (Class<List<Object>>) (Object) List.class,
        context -> {
          if (context.field() == null) return null;
          Class<?> fieldType = (Class<?>)
              ((ParameterizedType) context.field().getGenericType()).getActualTypeArguments()[0];
          var e = registry.getEntry(fieldType);

          return new NestedListListEntry<>(
              TextUtil.translatable(context.i18n()),
              context.value(),
              false,
              null,
              objects -> context.consumer().accept(objects),
              def(context),
              AutoConfigScreen.RESET_BUTTON_KEY,
              true,
              false,
              (o, entry) -> {
                AutoConfigScreen.EntryContext innerCtx = new AutoConfigScreen.EntryContext<>(
                    fieldType,
                    o == null ? e.def().apply(fieldType) : o,
                    e.def().apply(fieldType),
                    o1 -> {},
                    context.i18n(),
                    context.field(),
                    null);
                return e.function().getEntry(innerCtx);
              });
        },
        c -> new ArrayList<>());

    registry.register(
        (Class<Enum<?>>) (Object) Enum.class,
        context -> {
          List<Enum<?>> enums =
              Arrays.asList((Enum<?>[]) context.field().getType().getEnumConstants());

          return registry
              .entryBuilder()
              .startDropdownMenu(
                  TextUtil.translatable(context.i18n()),
                  DropdownMenuBuilder.TopCellElementBuilder.of(
                      context.value(),
                      s -> {
                        for (Enum<?> anEnum : enums) {
                          if (DEFAULT_NAME_PROVIDER.apply(anEnum).getString().equals(s))
                            return anEnum;
                        }
                        return null;
                      },
                      DEFAULT_NAME_PROVIDER))
              .setSelections(enums)
              .setDefaultValue(context.def())
              .setSaveConsumer(context.consumer())
              .build();
        },
        c -> (Enum<?>) c.getEnumConstants()[0]);

    registry.register(
        Object.class,
        context -> {
          String classI13n;
          if (context.module() == null) {
            String remainingI13n =
                context.i18n().substring(0, context.i18n().indexOf(".option") + ".option".length());
            classI13n = String.format("%s.%s", remainingI13n, context.type().getSimpleName());
          } else {
            classI13n = context.i18n();
          }

          List<AbstractConfigListEntry<?>> builder = new ArrayList<>();
          List<Field> fields = new ArrayList<>(Arrays.asList(context.type().getFields()));
          fields.removeIf(field -> field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class));

          for (Field field : fields) {
            String iI13n;
            if (context.module() == null) {
              iI13n = String.format("%s.%s", classI13n, field.getName());
            } else {
              iI13n = String.format("%s.option.%s", classI13n, field.getName());
            }
            var e = registry.getEntry(field.getType());
            var innerCtx = new AutoConfigScreen.EntryContext<>(
                field.getType(),
                getField(field, context.value()),
                getField(field, context.def()),
                object -> {
                  setField(field, context.value(), object);
                  context.consumer().accept(context.value());
                },
                iI13n,
                field,
                context.module());
            builder.add(e.function().getEntry(Utilities.cast(innerCtx)));
          }
          return new MultiElementListEntry<>(
              TextUtil.translatable(classI13n), context.value(), builder, false);
        },
        c -> ExceptionUtil.supply(() -> c.getConstructor().newInstance()));

    forRegistry(registry, Item.class, BuiltInRegistries.ITEM);
    forRegistry(registry, Block.class, BuiltInRegistries.BLOCK);
    forRegistry(
        registry,
        MobEffect.class,
        BuiltInRegistries.MOB_EFFECT,
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.REGENERATION));
  }

  private static <T> void forRegistry(
      AutoConfigScreen registry, Class<T> type, DefaultedRegistry<T> contentRegistry) {
    registry.register(
        type,
        context -> new RegistryListEntry<>(
            i18n(context),
            contentRegistry,
            contentRegistry.getDefaultKey(),
            context.value(),
            AutoConfigScreen.RESET_BUTTON_KEY,
            def(context),
            context.consumer(),
            null,
            false),
        c -> contentRegistry.get(contentRegistry.getDefaultKey()));
  }

  private static <T> void forRegistry(
      AutoConfigScreen registry, Class<T> type, Registry<T> contentRegistry, ResourceLocation def) {
    registry.register(
        type,
        context -> new RegistryListEntry<>(
            i18n(context),
            contentRegistry,
            def,
            context.value(),
            AutoConfigScreen.RESET_BUTTON_KEY,
            def(context),
            context.consumer(),
            null,
            false),
        c -> contentRegistry.get(def));
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

  private static <T> @Nullable Supplier<T> def(AutoConfigScreen.EntryContext<T> context) {
    T defaultValue = context.def();
    return (defaultValue == null || context.module() == null) ? null : () -> defaultValue;
  }

  private static Component i18n(AutoConfigScreen.EntryContext<?> context) {
    return (context.i18n().isBlank() || context.module() == null)
        ? TextUtil.empty()
        : TextUtil.translatable(context.i18n());
  }
}
