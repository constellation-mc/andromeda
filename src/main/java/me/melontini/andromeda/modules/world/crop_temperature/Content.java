package me.melontini.andromeda.modules.world.crop_temperature;

import me.melontini.andromeda.common.annotations.GameRule;
import me.melontini.andromeda.common.registries.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Content {

    @GameRule
    public static GameRules.Key<GameRules.BooleanRule> affectBoneMeal;

    public static void init(PlantTemperature module) {
        GameRuleRegistry.register(GameRuleBuilder.name(module, "affectBoneMeal"), GameRuleBuilder.category(module),
                GameRuleBuilder.booleanRule(() -> module.config().affectBoneMeal));
    }
}
