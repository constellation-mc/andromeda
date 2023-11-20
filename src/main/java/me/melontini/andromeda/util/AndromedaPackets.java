package me.melontini.andromeda.util;

import net.minecraft.util.Identifier;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class AndromedaPackets {

    public static final Identifier JUKEBOX_START_PLAYING = new Identifier(MODID, "jukebox_start_playing");

    public static final Identifier JUKEBOX_STOP_PLAYING = new Identifier(MODID, "jukebox_stop_playing");

    public static final Identifier USED_CUSTOM_TOTEM = new Identifier(MODID, "used_custom_totem");

    public static final Identifier FLYING_STACK_LANDED = new Identifier(MODID, "flying_stack_landed");

    public static final Identifier COLORED_FLYING_STACK_LANDED = new Identifier(MODID, "colored_flying_stack_landed");

    public static final Identifier ADD_ONE_PARTICLE = new Identifier(MODID, "add_one_particle");

    public static final Identifier EXPLODE_BOAT_ON_SERVER = new Identifier(MODID, "explode_boat_on_server");
}
