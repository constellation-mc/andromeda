package me.melontini.andromeda.modules.blocks.campfire_effects;

import me.melontini.dark_matter.api.base.util.MakeSure;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public class PotionUtil {

    public static @NotNull StatusEffect getStatusEffect(Identifier id) {
        StatusEffect effect = Registry.STATUS_EFFECT.get(id);
        MakeSure.notNull(effect, "(Andromeda) Couldn't get StatusEffect from identifier: " + id);
        return effect;
    }
}
