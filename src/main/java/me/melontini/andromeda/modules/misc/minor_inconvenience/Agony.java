package me.melontini.andromeda.modules.misc.minor_inconvenience;

import net.minecraft.entity.damage.DamageSource;

import static me.melontini.andromeda.registries.Common.run;

public class Agony {
    public static DamageSource AGONY;

    public static void init() {
        Agony.AGONY = run(() -> new DamageSource("andromeda_agony"), "minorInconvenience");
    }
}
