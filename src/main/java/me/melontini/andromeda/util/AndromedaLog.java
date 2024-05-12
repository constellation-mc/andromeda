package me.melontini.andromeda.util;

import com.google.common.base.Splitter;
import lombok.experimental.UtilityClass;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.Utilities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@UtilityClass
public class AndromedaLog {

    private static final Splitter SPLITTER = Splitter.on(".");

    public static @NotNull PrependingLogger factory() {
        Class<?> cls = Utilities.getCallerClass(2).orElseThrow();
        List<String> split = SPLITTER.splitToList(cls.getName());

        String caller = split.get(split.size() - 1);
        caller = "Andromeda/" + caller;
        if (cls.getName().startsWith("net.minecraft.")) caller += "@Mixin";

        boolean prefix = (!Utilities.isDev() && CommonValues.platform() != CommonValues.Platform.CONNECTOR);
        return PrependingLogger.get(caller, logger -> prefix ? "(" + logger.getName() + ") " : "");
    }
}
