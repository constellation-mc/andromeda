package me.melontini.andromeda.modules.blocks.campfire_effects;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

public class Config extends BasicConfig {

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
