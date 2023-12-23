package me.melontini.andromeda.modules.blocks.campfire_effects;

import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class PotionUtil {

    //TODO reset to default;
    public static @NotNull StatusEffect getStatusEffect(Identifier id) {
        StatusEffect effect = CommonRegistries.statusEffects().get(id);
        if (effect == null) {
            throw new AndromedaException(false, "(Andromeda) Couldn't get StatusEffect from identifier: " + id);
        }
        return effect;
    }
}
