package me.melontini.andromeda.modules.world.moist_control;

import me.melontini.andromeda.common.annotations.GameRule;
import me.melontini.andromeda.common.registries.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Content {

    @GameRule
    public static GameRules.Key<GameRules.IntRule> customMoisture;

    public static void init(MoistControl module) {
        customMoisture = GameRuleRegistry.register(GameRuleBuilder.name(module, "customMoisture"),
                GameRuleBuilder.category(module), GameRuleBuilder.intRule(() -> module.config().customMoisture));
    }
}
