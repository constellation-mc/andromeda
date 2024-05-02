package me.melontini.andromeda.modules.blocks.campfire_effects;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import java.util.List;

@ModuleInfo(name = "campfire_effects", category = "blocks", environment = Environment.SERVER)
public class CampfireEffects extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    CampfireEffects() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends GameConfig {
        public boolean affectsPassive = true;
        public NumberIntermediary effectsRange = NumberIntermediary.of(10);
        public List<Effect> effectList = Lists.newArrayList(new Effect());

        @ToString
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Effect {
            public StatusEffect identifier = StatusEffects.REGENERATION;
            public NumberIntermediary amplifier = NumberIntermediary.of(0d);
        }
    }
}
