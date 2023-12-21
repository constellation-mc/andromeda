package me.melontini.andromeda.common.registries;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.melontini.andromeda.base.Module;
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
