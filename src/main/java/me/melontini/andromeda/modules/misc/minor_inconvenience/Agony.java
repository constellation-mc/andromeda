package me.melontini.andromeda.modules.misc.minor_inconvenience;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.run;

public class Agony {
    public static RegistryKey<DamageType> AGONY;

    public static void init() {
        Agony.AGONY = run(() -> RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("agony")), "minorInconvenience");
    }
}
