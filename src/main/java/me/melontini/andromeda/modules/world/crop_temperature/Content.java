package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.common.registries.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Content {

    public static GameRules.Key<GameRules.BooleanRule> AFFECT_BONE_MEAL;

    public static void init(PlantTemperature m) {
        AFFECT_BONE_MEAL = GameRuleRegistry.register(GameRuleBuilder.name(m, "affectBoneMeal"), GameRuleBuilder.category(m), GameRuleBuilder.booleanRule(() ->
                m.config().affectBoneMeal));
    }
}
