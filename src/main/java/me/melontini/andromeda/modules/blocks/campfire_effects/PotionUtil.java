package me.melontini.andromeda.modules.blocks.campfire_effects;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class PotionUtil {

    public static @NotNull StatusEffect getStatusEffect(Identifier id) {
        StatusEffect effect = CommonRegistries.statusEffects().get(id);
        if (effect == null) {
            ModuleManager.get().getModule(CampfireEffects.class).ifPresent(effects -> {
                effects.config().effectList = effects.defaultConfig().effectList;
                effects.save();
            });
            throw new AndromedaException(false, "(Andromeda) Couldn't get StatusEffect from identifier: " + id);
        }
        return effect;
    }
}
