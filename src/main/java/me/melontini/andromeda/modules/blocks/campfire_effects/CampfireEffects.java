package me.melontini.andromeda.modules.blocks.campfire_effects;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "campfire_effects", category = "blocks", environment = Environment.SERVER)
public class CampfireEffects extends Module<CampfireEffects.Config> {

    public static class Config extends BaseConfig {

        public boolean affectsPassive = true;

        @ConfigEntry.Category("blocks")
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
