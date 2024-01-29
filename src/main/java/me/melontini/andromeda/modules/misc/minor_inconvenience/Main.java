package me.melontini.andromeda.modules.misc.minor_inconvenience;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {
    public static RegistryKey<DamageType> AGONY;

    Main() {
        Main.AGONY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("agony"));
    }
}
