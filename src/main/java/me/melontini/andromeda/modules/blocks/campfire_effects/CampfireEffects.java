package me.melontini.andromeda.modules.blocks.campfire_effects;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@ModuleTooltip
@FeatureEnvironment(Environment.SERVER)
public class CampfireEffects implements Module<CampfireEffects.Config> {

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean affectsPassive = true;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        public int effectsRange = 10;

        @ConfigEntry.Category("blocks")
        public List<Effect> effectList = Arrays.asList(new Effect("minecraft:regeneration", 0));

        @AllArgsConstructor
        @NoArgsConstructor
        public static class Effect {
            public String identifier;
            public int amplifier;
        }
    }
}
