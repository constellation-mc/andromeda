package me.melontini.andromeda.util;

import me.melontini.andromeda.config.Config;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.Utilities;

public class AndromedaLog {

    private static final PrependingLogger LOGGER = PrependingLogger.get("Andromeda", logger -> {
        StackWalker.StackFrame frame = Utilities.STACK_WALKER.walk(s -> s.skip(3).findFirst().orElse(null));
        String[] split = frame.getClassName().split("\\.");
        String caller = split[split.length - 1];
        if (frame.getClassName().startsWith("net.minecraft.")) caller = caller + "@Mixin";
        return ((!Utilities.isDev() && SharedConstants.PLATFORM != SharedConstants.Platform.CONNECTOR) ?
                "(" + logger.getName() + ") " : "") + "[" + caller + "] ";
    });

    public static PrependingLogger factory() {
        Class<?> cls = Utilities.getCallerClass(2);
        String[] split = cls.getName().split("\\.");

        String caller = split[split.length - 1];
        caller = "Andromeda/" + caller;
        if (cls.getName().startsWith("net.minecraft.")) caller = caller + "@Mixin";

        return PrependingLogger.get(caller, logger -> (!Utilities.isDev() && SharedConstants.PLATFORM != SharedConstants.Platform.CONNECTOR) ?
                "(" + logger.getName() + ") " : "");
    }

    public static void devInfo(String msg) {
        if (Config.get().debugMessages) {
            LOGGER.info(msg);
        }
    }
    public static void devInfo(Object object) {
        if (Config.get().debugMessages) {
            LOGGER.info(object);
        }
    }
    public static void devInfo(String msg, Object... params) {
        if (Config.get().debugMessages) {
            LOGGER.info(msg, params);
        }
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }

    public static void error(String msg, Throwable t) {
        LOGGER.error(msg, t);
    }

    public static void error(Object msg) {
        LOGGER.error(msg);
    }

    public static void error(String msg, Object... args) {
        LOGGER.error(msg, args);
    }

    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    public static void warn(String msg, Throwable t) {
        LOGGER.warn(msg, t);
    }

    public static void warn(Object msg) {
        LOGGER.warn(msg);
    }

    public static void warn(String msg, Object... args) {
        LOGGER.warn(msg, args);
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void info(String msg, Throwable t) {
        LOGGER.info(msg, t);
    }

    public static void info(Object msg) {
        LOGGER.info(msg);
    }

    public static void info(String msg, Object... args) {
        LOGGER.info(msg, args);
    }
}
