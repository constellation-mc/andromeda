package me.melontini.andromeda.common.client.config;

import com.google.gson.JsonPrimitive;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.TranslationKeyProvider;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;
import me.melontini.andromeda.util.commander.number.LongIntermediary;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.melontini.andromeda.common.client.config.NewAutoConfigScreen.ENTRY_BUILDER;
import static me.melontini.andromeda.common.client.config.NewAutoConfigScreen.builder;

public class NewProviders {

    private static final Function<Enum<?>, Text> DEFAULT_NAME_PROVIDER = (t) -> {
        if (t instanceof TranslationKeyProvider provider) {//DIY API
            return provider.getTranslationKey().map(TextUtil::translatable).orElseGet(() -> TextUtil.literal(t.name()));
        }
        return TextUtil.literal(t.name());
    };

    static void init() {
        Set.of(int.class, Integer.class).forEach(cl -> builder(cl, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startIntField(i18n(i18n, context), value)
                        .setDefaultValue((def == null || context.generic()) ? null : () -> def)
                        .setSaveConsumer(setter).build(), (c) -> 0).build());

        Set.of(boolean.class, Boolean.class).forEach(cl -> builder(cl, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startBooleanToggle(i18n(i18n, context), value)
                        .setDefaultValue((def == null || context.generic()) ? null : () -> def)
                        .setSaveConsumer(setter).build(), (c) -> false).build());

        Set.of(double.class, Double.class).forEach(cl -> builder(cl, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startDoubleField(i18n(i18n, context), value)
                        .setDefaultValue((def == null || context.generic()) ? null : () -> def)
                        .setSaveConsumer(setter).build(), (c) -> 0d).build());

        builder(String.class, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startStrField(i18n(i18n, context), value)
                        .setDefaultValue((def == null || context.generic()) ? null : () -> def)
                        .setSaveConsumer(setter).build(), (c) -> "").build();

        builder(Identifier.class, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startStrField(i18n(i18n, context), value.toString())
                        .setDefaultValue((def == null || context.generic()) ? null : def::toString)
                        .setSaveConsumer(s -> setter.accept(new Identifier(s))).build(), (c) -> new Identifier(""))
                .converter(Identifier::new, Identifier::toString)
                .errorSupplier((String s) -> {
                    var r = Identifier.validate(s);
                    if (r.error().isPresent())
                        return Optional.of(TextUtil.literal(r.error().orElseThrow().message()));
                    return Optional.empty();
                }).build();

        NewAutoConfigScreen.<List<Object>>builder(aClass -> List.class == aClass, (type, value, def, setter, i18n, context) -> {
            if (context.field() == null) return null;
            Class<?> fieldType = (Class<?>) ((ParameterizedType) context.field().getGenericType()).getActualTypeArguments()[0];
            var e = NewAutoConfigScreen.getProviders().entrySet().stream().filter(me -> me.getKey().test(fieldType)).findFirst().map(Map.Entry::getValue).orElse(NewAutoConfigScreen.defaultProvider());
            NewAutoConfigScreen.Context context1 = context.withGeneric(true);
            var r = new NestedListListEntry<>(TextUtil.translatable(i18n), value.stream().map(object -> e.converters().toBase().apply(object)).toList(), false, null,
                    objects -> setter.accept(objects.stream().map(object -> e.converters().fromBase().apply(object)).toList()),
                    def == null ? null : () -> def.stream().map(object -> e.converters().toBase().apply(object)).toList(), ENTRY_BUILDER.getResetButtonKey(), true, false,
                    (object, entry) -> (AbstractConfigListEntry<Object>) e.provider().getEntry(fieldType, object == null ? e.def().apply(fieldType) : e.converters().fromBase().apply(object), e.def().apply(fieldType), o -> {
                    }, i18n, context1));

            r.setCellErrorSupplier(e.errorSupplier());
            return r;
        }, aClass -> new ArrayList<>()).build();

        NewAutoConfigScreen.<Enum<?>>builder(aClass -> aClass.isEnum() || Enum.class.isAssignableFrom(aClass), (type, value, def, setter, i18n, context) -> {
            List<Enum<?>> enums = Arrays.asList((Enum<?>[]) context.field().getType().getEnumConstants());

            return ENTRY_BUILDER.startDropdownMenu(TextUtil.translatable(i18n), DropdownMenuBuilder.TopCellElementBuilder.of(value, (str) -> {
                        for (Enum<?> anEnum : enums) {
                            if (DEFAULT_NAME_PROVIDER.apply(anEnum).getString().equals(str)) return anEnum;
                        }
                        return null;
                    }, DEFAULT_NAME_PROVIDER))
                    .setSelections(enums).setDefaultValue(def)
                    .setSaveConsumer(setter)
                    .build();
        }, aClass -> Exceptions.supply(() -> ((Enum<?>[]) aClass.getMethod("values").invoke(null)))[0])
                .build();

        forRegistry(Item.class, Registries.ITEM);
        forRegistry(Block.class, Registries.BLOCK);
        forRegistry(StatusEffect.class, Registries.STATUS_EFFECT, () -> StatusEffects.REGENERATION);

        Runnable commander = Support.support("commander", () -> NewProviders::commanderIntermediaries, () -> NewProviders::constantIntermediaries);
        commander.run();
    }

    private static Text i18n(String i18n, NewAutoConfigScreen.Context context) {
        return (i18n.isBlank() || context.generic()) ? TextUtil.empty() : TextUtil.translatable(i18n);
    }

    private static <T> void forRegistry(Class<T> type, DefaultedRegistry<T> registry) {
        T t = registry.get(registry.getDefaultId());
        forRegistry(type, registry, () -> t);
    }

    private static <T> void forRegistry(Class<T> type, Registry<T> registry, Supplier<T> supplier) {
        builder(type, (type1, value, def, setter, i18n, context) -> {
            var val = Objects.requireNonNull(registry.getId(value)).toString();
            var newDef = Objects.requireNonNull(registry.getId(def)).toString();

            return ENTRY_BUILDER.startStrField(i18n(i18n, context), val)
                    .setDefaultValue((def == null || context.generic()) ? null : () -> newDef)
                    .setSaveConsumer(s -> setter.accept(registry.get(new Identifier(s)))).build();
        }, c -> supplier.get())
                .converter((String s) -> registry.get(new Identifier(s)), t -> Objects.requireNonNull(registry.getId(t)).toString())
                .errorSupplier((String s) -> {
                    var r = Identifier.validate(s);
                    if (r.error().isPresent())
                        return Optional.of(TextUtil.literal(r.error().orElseThrow().message()));
                    if (!registry.containsId(r.result().orElseThrow()))
                        return Optional.of(TextUtil.translatable("text.cloth-config.error_cannot_save"));
                    return Optional.empty();
                }).build();
    }

    private static void constantIntermediaries() {
        builder(DoubleIntermediary.class, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startDoubleField(i18n(i18n, context), value.asDouble(null))
                        .setDefaultValue((def == null || context.generic()) ? null : () -> def.asDouble(null))
                        .setSaveConsumer((newValue) -> setter.accept(DoubleIntermediary.of(newValue))).build(), (c) -> DoubleIntermediary.of(0))
                .converter(DoubleIntermediary::of, i -> Andromeda.GAME_HANDLER.getGson().toJsonTree(i, DoubleIntermediary.class).getAsJsonPrimitive().getAsDouble())
                .build();

        builder(LongIntermediary.class, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startLongField(i18n(i18n, context), value.asLong(null))
                        .setDefaultValue((def == null || context.generic()) ? null : () -> def.asLong(null))
                        .setSaveConsumer((newValue) -> setter.accept(LongIntermediary.of(newValue))).build(), (c) -> LongIntermediary.of(0))
                .converter(LongIntermediary::of, i -> Andromeda.GAME_HANDLER.getGson().toJsonTree(i, LongIntermediary.class).getAsJsonPrimitive().getAsLong())
                .build();

        builder(BooleanIntermediary.class, (type, value, def, setter, i18n, context) ->
                ENTRY_BUILDER.startBooleanToggle(i18n(i18n, context), value.asBoolean(null))
                        .setDefaultValue((def == null || context.generic()) ? null : () -> def.asBoolean(null))
                        .setSaveConsumer((newValue) -> setter.accept(BooleanIntermediary.of(newValue))).build(), (c) -> BooleanIntermediary.of(false))
                .converter(BooleanIntermediary::of, i -> Andromeda.GAME_HANDLER.getGson().toJsonTree(i, BooleanIntermediary.class).getAsJsonPrimitive().getAsBoolean())
                .build();
    }

    private static void commanderIntermediaries() {
        Function<String, DoubleIntermediary> toNumber = (String str) -> {
            try {
                return DoubleIntermediary.of(new BigDecimal(str).doubleValue());
            } catch (Exception e) {
                return Andromeda.GAME_HANDLER.getGson().fromJson(new JsonPrimitive(str), DoubleIntermediary.class);
            }
        };
        builder(DoubleIntermediary.class, (type, value, def, setter, i18n, context) -> {
            var p = Andromeda.GAME_HANDLER.getGson().toJsonTree(value, DoubleIntermediary.class).getAsJsonPrimitive();
            var p1 = (def == null || context.generic()) ? null : Andromeda.GAME_HANDLER.getGson().toJsonTree(def, DoubleIntermediary.class).getAsJsonPrimitive();

            return ENTRY_BUILDER.startStrField(i18n(i18n, context), p.getAsString()).setDefaultValue(p1 == null ? null : p1::getAsString)
                    .setSaveConsumer((newValue) -> setter.accept(toNumber.apply(newValue))).build();
        }, (c) -> DoubleIntermediary.of(0))
                .converter(toNumber, i -> Andromeda.GAME_HANDLER.getGson().toJsonTree(i, DoubleIntermediary.class).getAsJsonPrimitive().getAsString())
                .errorSupplier((String s) -> {
                    try {
                        toNumber.apply(s);
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.of(TextUtil.literal(getExceptionMessage(e)));
                    }
                }).build();

        Function<String, LongIntermediary> toLong = (String str) -> {
            try {
                return LongIntermediary.of(new BigDecimal(str).longValue());
            } catch (Exception e) {
                return Andromeda.GAME_HANDLER.getGson().fromJson(new JsonPrimitive(str), LongIntermediary.class);
            }
        };
        builder(LongIntermediary.class, (type, value, def, setter, i18n, context) -> {
            var p = Andromeda.GAME_HANDLER.getGson().toJsonTree(value, LongIntermediary.class).getAsJsonPrimitive();
            var p1 = (def == null || context.generic()) ? null : Andromeda.GAME_HANDLER.getGson().toJsonTree(def, LongIntermediary.class).getAsJsonPrimitive();

            return ENTRY_BUILDER.startStrField(i18n(i18n, context), p.getAsString()).setDefaultValue(p1 == null ? null : p1::getAsString)
                    .setSaveConsumer((newValue) -> setter.accept(toLong.apply(newValue))).build();
        }, (c) -> LongIntermediary.of(0))
                .converter(toLong, i -> Andromeda.GAME_HANDLER.getGson().toJsonTree(i, LongIntermediary.class).getAsJsonPrimitive().getAsString())
                .errorSupplier((String s) -> {
                    try {
                        toLong.apply(s);
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.of(TextUtil.literal(getExceptionMessage(e)));
                    }
                }).build();

        TreeMap<String, BooleanIntermediary> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        var trueInt = BooleanIntermediary.of(true);
        var falseInt = BooleanIntermediary.of(false);
        map.put("true", trueInt);
        map.put("false", falseInt);
        map.put("yes", trueInt);
        map.put("no", falseInt);
        map.put("on", trueInt);
        map.put("off", falseInt);

        Function<String, BooleanIntermediary> toBoolean = (String str) -> {
            var realNewValue = map.get(str);
            if (realNewValue != null) return realNewValue;

            return Andromeda.GAME_HANDLER.getGson().fromJson(new JsonPrimitive(str), BooleanIntermediary.class);
        };
        builder(BooleanIntermediary.class, (type, value, def, setter, i18n, context) -> {
            var p = Andromeda.GAME_HANDLER.getGson().toJsonTree(value, BooleanIntermediary.class).getAsJsonPrimitive();
            var p1 = (def == null || context.generic()) ? null : Andromeda.GAME_HANDLER.getGson().toJsonTree(def, BooleanIntermediary.class).getAsJsonPrimitive();

            return ENTRY_BUILDER.startStrField(i18n(i18n, context), p.getAsString()).setDefaultValue(p1 == null ? null : p1::getAsString)
                    .setSaveConsumer((newValue) -> setter.accept(toBoolean.apply(newValue))).build();
        }, (c) -> BooleanIntermediary.of(false))
                .converter(toBoolean, i -> Andromeda.GAME_HANDLER.getGson().toJsonTree(i, BooleanIntermediary.class).getAsJsonPrimitive().getAsString())
                .errorSupplier((String s) -> {
                    try {
                        toBoolean.apply(s);
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.of(TextUtil.literal(getExceptionMessage(e)));
                    }
                }).build();
    }

    private static String getExceptionMessage(Exception e) {
        Throwable unwrapped = Exceptions.unwrap(e);
        String msg = untilNotNull(unwrapped);
        return msg == null ? unwrapped.getClass().getSimpleName() : msg;
    }

    private static String untilNotNull(Throwable throwable) {
        if (throwable.getMessage() == null) return throwable.getCause() != null ? untilNotNull(throwable) : null;
        return throwable.getMessage();
    }
}
