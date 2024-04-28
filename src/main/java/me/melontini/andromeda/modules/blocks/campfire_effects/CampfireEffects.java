package me.melontini.andromeda.modules.blocks.campfire_effects;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.ConfigGsonEvent;
import me.melontini.andromeda.base.util.ConfigHandler;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;

import java.util.List;

@ModuleInfo(name = "campfire_effects", category = "blocks", environment = Environment.SERVER)
public class CampfireEffects extends Module<CampfireEffects.Config> {

    CampfireEffects() {
        ConfigGsonEvent.BUS.listen(builder -> builder.registerTypeAdapter(StatusEffect.class, ConfigHandler.context(Registries.STATUS_EFFECT.getCodec())));
    }

    public static class Config extends BaseConfig {

        public boolean affectsPassive = true;

        @ConfigEntry.Category("blocks")
        public int effectsRange = 10;

        @ConfigEntry.Category("blocks")
        public List<Effect> effectList = Lists.newArrayList(new Effect());

        @AllArgsConstructor
        @NoArgsConstructor
        public static class Effect {
            public StatusEffect effect = StatusEffects.REGENERATION;
            public int amplifier = 0;
        }
    }
}
