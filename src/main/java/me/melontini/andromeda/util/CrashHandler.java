package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.classes.Context;
import me.melontini.dark_matter.api.crash_handler.Crashlytics;
import me.melontini.dark_matter.api.crash_handler.Prop;
import me.melontini.dark_matter.api.crash_handler.uploading.Mixpanel;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Set;

public class CrashHandler {

    private static final Mixpanel MIXPANEL = Mixpanel.get(new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE=")), true);

    private static boolean findAndromedaInTrace(Throwable cause) {
        if (cause instanceof AndromedaException e && e.shouldReport()) return true;

        for (StackTraceElement element : cause.getStackTrace()) {
            if (element.isNativeMethod()) continue;
            String cls = element.getClassName();
            if (cls.startsWith("me.melontini.andromeda.")) return true;
            if (cls.startsWith("net.minecraft.")) {
                String mthd = element.getMethodName();
                return (mthd.contains("$andromeda$") || mthd.contains(".andromeda$"));
            }
        }
        return cause.getCause() != null && findAndromedaInTrace(cause.getCause());
    }

    public static void handleCrash(Throwable cause, Context context) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() || !Config.get().sendCrashReports) return;
        if (context.get(IMixinInfo.class, Crashlytics.Keys.MIXIN_INFO).map(info -> info.getClassName().startsWith("me.melontini.andromeda")).orElse(false) || findAndromedaInTrace(cause)) {
            AndromedaLog.warn("Found Andromeda in trace, collecting and uploading crash report...");
            JsonObject object = new JsonObject();

            String message = "Something terrible happened!";
            if (context.get(Object.class, Crashlytics.Keys.CRASH_REPORT).isPresent()) {
                message = getFromCrashReport(context);
            }

            //fill trace.
            JsonArray stackTrace = new JsonArray();
            for (String string : getCauseAsString(cause, message).lines().flatMap(s -> StringUtil.wrapLines(s, 190).lines()).toList())
                stackTrace.add(string);
            object.add("stackTrace", stackTrace);

            MIXPANEL.attachProps(object, Prop.ENVIRONMENT, Prop.OS, Prop.JAVA_VERSION, Prop.JAVA_VENDOR);
            object.addProperty("platform", CommonValues.platform().toString().toLowerCase());
            object.addProperty("bootstrap_status", Bootstrap.getStatus().toString());

            JsonArray mods = new JsonArray();
            Set<String> importantMods = Sets.newHashSet("andromeda", "minecraft", "fabric-api", "fabricloader", "connectormod", "forge");

            for (String importantMod : importantMods) {
                FabricLoader.getInstance().getModContainer(importantMod).ifPresent(mod -> mods.add(importantMod + " (" + mod.getMetadata().getVersion().getFriendlyString() + ")"));
            }

            object.add("mods", mods);

            MIXPANEL.upload(new Mixpanel.Context("Crash", object)).handle((unused, throwable) -> {
                if (throwable != null)
                    AndromedaLog.error("Failed to upload crash report! {}: {}", throwable.getClass().getSimpleName(), throwable.getMessage());
                return null;
            });
        }
    }

    private static String getFromCrashReport(Context context) {
        return context.get(Object.class, Crashlytics.Keys.CRASH_REPORT)
                .map(CrashReport.class::cast).map(CrashReport::getMessage).orElse("null");
    }

    private static String getCauseAsString(Throwable cause, String message) {
        Throwable throwable = getThrowable(cause, message);

        try(var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            return "Failed to get cause: " + e.getMessage();
        }
    }

    private static Throwable getThrowable(Throwable cause, String message) {
        Throwable throwable = cause;
        if (throwable.getMessage() == null) {
            if (throwable instanceof NullPointerException) {
                throwable = new NullPointerException(message);
            } else if (throwable instanceof StackOverflowError) {
                throwable = new StackOverflowError(message);
            } else if (throwable instanceof OutOfMemoryError) {
                throwable = new OutOfMemoryError(message);
            }

            throwable.setStackTrace(cause.getStackTrace());
        }
        return throwable;
    }
}
