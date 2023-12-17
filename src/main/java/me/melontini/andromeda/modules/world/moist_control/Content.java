package me.melontini.andromeda.modules.world.moist_control;

import me.melontini.andromeda.common.registries.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Content {

    public static GameRules.Key<GameRules.IntRule> CUSTOM_MOISTURE;

    public static void init(MoistControl m) {
        CUSTOM_MOISTURE = GameRuleRegistry.register(GameRuleBuilder.name(m, "customMoisture"), GameRuleBuilder.category(m), GameRuleBuilder.intRule(() ->
                m.config().customMoisture));
    }
}
