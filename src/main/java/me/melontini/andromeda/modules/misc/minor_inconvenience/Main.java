package me.melontini.andromeda.modules.misc.minor_inconvenience;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import static me.melontini.andromeda.common.Andromeda.id;

public final class Main {
    public static final RegistryKey<DamageType> AGONY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("agony"));
}
