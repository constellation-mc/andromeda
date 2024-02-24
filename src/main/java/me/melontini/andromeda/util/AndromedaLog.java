package me.melontini.andromeda.util;

import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.Utilities;

public class AndromedaLog {

    private static final boolean prefix = (!Utilities.isDev() && CommonValues.platform() != CommonValues.Platform.CONNECTOR);

    public static PrependingLogger factory() {
        Class<?> cls = Utilities.getCallerClass(2);
        String[] split = cls.getName().split("\\.");

        String caller = split[split.length - 1];
        caller = "Andromeda/" + caller;
        if (cls.getName().startsWith("net.minecraft.")) caller += "@Mixin";

        return PrependingLogger.get(caller, logger -> prefix ? "(" + logger.getName() + ") " : "");
    }
}
