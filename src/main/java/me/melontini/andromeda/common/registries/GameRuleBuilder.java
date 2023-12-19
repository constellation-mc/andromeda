package me.melontini.andromeda.common.registries;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.melontini.andromeda.base.Module;
import me.melontini.dark_matter.api.config.ConfigManager;
import me.melontini.dark_matter.api.config.interfaces.Option;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public class GameRuleBuilder {

    private static final Map<Module<?>, CustomGameRuleCategory> CATEGORIES = new IdentityHashMap<>();

    public static CustomGameRuleCategory category(Module<?> m) {
        return CATEGORIES.computeIfAbsent(m, module -> new CustomGameRuleCategory(Common.id(m.meta().id()),
                TextUtil.translatable("config.andromeda.%s".formatted(module.meta().dotted()))
                        .formatted(Formatting.BOLD, Formatting.YELLOW)
                        .append(TextUtil.translatable("andromeda.game_rule.category")
                                .formatted(Formatting.BOLD, Formatting.YELLOW))));
    }

    public static String name(Module<?> m, String rule) {
        return "andromeda:" + m.meta().dotted() + ":" + rule;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static GameRules.Type<?> forOption(ConfigManager<?> manager, Option option) {
        if (option.type() == int.class || option.type() == Integer.class) {
            return intRule(() -> (int) option.get(manager.getConfig()));
        }

        if (option.type() == boolean.class || option.type() == Boolean.class) {
            return booleanRule(() -> (boolean) option.get(manager.getConfig()));
        }

        throw new IllegalStateException("Illegal option type! %s (%s)".formatted(option.name(), option.type()));
    }

    public static GameRules.Type<GameRules.IntRule> intRule(IntSupplier defaultValue) {
        return new GameRules.Type<>(
                () -> IntegerArgumentType.integer(Integer.MIN_VALUE, Integer.MAX_VALUE),
                type -> new GameRules.IntRule(type, defaultValue.getAsInt()),
                (server, intRule) -> {
                },
                GameRules.Visitor::visitInt
        );
    }

    public static GameRules.Type<GameRules.BooleanRule> booleanRule(BooleanSupplier defaultValue) {
        return new GameRules.Type<>(
                BoolArgumentType::bool,
                type -> new GameRules.BooleanRule(type, defaultValue.getAsBoolean()),
                (server, intRule) -> {
                },
                GameRules.Visitor::visitBoolean
        );
    }
}
