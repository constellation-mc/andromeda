package me.melontini.andromeda.modules.misc.minor_inconvenience;

import net.minecraft.entity.damage.DamageSource;

public class Agony {
    public static DamageSource AGONY;

    public static void init() {
        Agony.AGONY = new DamageSource("andromeda_agony");
    }
}
