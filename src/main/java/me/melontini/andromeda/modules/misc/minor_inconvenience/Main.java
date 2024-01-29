package me.melontini.andromeda.modules.misc.minor_inconvenience;

import net.minecraft.entity.damage.DamageSource;

public class Main {
    public static DamageSource AGONY;

    Main() {
        Main.AGONY = new DamageSource("andromeda_agony");
    }
}
