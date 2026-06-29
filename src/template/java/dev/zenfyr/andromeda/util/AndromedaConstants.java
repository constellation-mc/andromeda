package dev.zenfyr.andromeda.util;

public class AndromedaConstants {

    public static final String MODID = "${mod_id}";
    public static final String VERSION = "${mod_version}";
    public static final String MINECRAFT_VERSION = "${minecraft_version}";

    public static String idString(String path) {
        return MODID + ":" + path;
    }
}
