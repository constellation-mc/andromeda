package me.melontini.andromeda.modules.blocks.campfire_effects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.events.LegacyConfigEvent;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "campfire_effects", category = "blocks", environment = Environment.SERVER)
public class CampfireEffects extends Module<CampfireEffects.Config> {

    CampfireEffects() {
        LegacyConfigEvent.BUS.listen(config -> {
            if (config.has("campfireTweaks")) {
                JsonObject campfireTweaks = config.getAsJsonObject("campfireTweaks");

                JsonOps.ifPresent(campfireTweaks, "campfireEffects", e -> this.config().enabled = e.getAsBoolean());
                JsonOps.ifPresent(campfireTweaks, "campfireEffectsPassive", e -> this.config().affectsPassive = e.getAsBoolean());
                JsonOps.ifPresent(campfireTweaks, "campfireEffectsRange", e -> this.config().effectsRange = e.getAsInt());

                JsonOps.ifPresent(campfireTweaks, "effectsList", element -> {
                    List<Config.Effect> effects = new ArrayList<>();
                    for (JsonElement e : element.getAsJsonArray()) {
                        JsonObject o = e.getAsJsonObject();
                        effects.add(new Config.Effect(o.get("identifier").getAsString(), o.get("amplifier").getAsInt()));
                    }
                    this.config().effectList = effects;
                });
            }
        });
    }

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
