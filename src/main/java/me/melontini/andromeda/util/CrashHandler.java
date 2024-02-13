package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.classes.Context;
import me.melontini.dark_matter.api.crash_handler.Crashlytics;
import me.melontini.dark_matter.api.crash_handler.uploading.Mixpanel;
import me.melontini.dark_matter.api.crash_handler.uploading.Uploader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.StringUtil;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class CrashHandler {

    private static final Mixpanel MIXPANEL = Mixpanel.get(new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE=")), true);
    private static final Set<String> IMPORTANT_MODS = Sets.newHashSet("andromeda", "minecraft", "fabric-api", "fabricloader", "connectormod", "forge");

    private static boolean shouldReportRecursive(Throwable cause) {
        if (cause instanceof AndromedaException e && !e.shouldReport()) return false;
        return cause.getCause() != null && shouldReportRecursive(cause.getCause());
    }

    public static Optional<JsonObject> traverse(Throwable cause) {
        if (cause instanceof AndromedaException e) {
            JsonObject s = e.getStatuses();
            if (e.getCause() != null) {
                traverse(e.getCause()).ifPresent(object -> s.add("cause", object));
            }
            return Optional.ofNullable(s);
        }
        if (cause.getCause() != null) return traverse(cause.getCause());
        return Optional.empty();
    }

    private static final Set<String> BAD_PREFIXES = Set.of(
            "me.melontini.andromeda.util.exceptions.AndromedaException", //run and Builder.build
            "jdk.internal.reflect.", //Most likely, accessors
            "com.sun.proxy.jdk.", //No source, useless
            "java.lang.invoke.MethodHandleProxies$" //Internal class
    );

    public static void sanitizeTrace(Throwable cause) {
        List<StackTraceElement> e = new ArrayList<>(Arrays.asList(cause.getStackTrace()));

        e.removeIf(el -> BAD_PREFIXES.stream().anyMatch(s -> el.getClassName().startsWith(s)));

        cause.setStackTrace(e.toArray(StackTraceElement[]::new));
        if (cause.getCause() != null) sanitizeTrace(cause.getCause());
    }

    public static void handleCrash(Throwable cause, Context context) {
        if (!Debug.Keys.FORCE_CRASH_REPORT_UPLOAD.isPresent()) {
            if (FabricLoader.getInstance().isDevelopmentEnvironment() || !AndromedaConfig.get().sendCrashReports)
                return;
        }

        if (!context.get(IMixinInfo.class, Crashlytics.Keys.MIXIN_INFO).map(info -> info.getClassName().startsWith("me.melontini.andromeda")).orElse(false) && !shouldReportRecursive(cause))
            return;
        AndromedaLog.warn("Found Andromeda in trace, collecting and uploading crash report...");

        sanitizeTrace(cause);

        JsonObject object = new JsonObject();
        //fill trace.
        JsonArray stackTrace = new JsonArray();
        for (String string : getCauseAsString(cause).lines().flatMap(s -> StringUtil.wrapLines(s, 190).lines()).toList())
            stackTrace.add(string);
        object.add("stackTrace", stackTrace);

        object.add("statuses", traverse(cause).orElseGet(AndromedaException::defaultStatuses));

        JsonArray mods = new JsonArray();
        for (String importantMod : IMPORTANT_MODS) {
            FabricLoader.getInstance().getModContainer(importantMod).ifPresent(mod -> mods.add(importantMod + " (" + mod.getMetadata().getVersion().getFriendlyString() + ")"));
        }
        object.add("mods", mods);

        if (context.get(Boolean.class, "andromeda:skip_service").orElse(false)) {
            upload(object);
        } else {
            Uploader.SERVICE.submit(() -> upload(object));
        }
    }

    private static void upload(JsonObject object) {
        MIXPANEL.upload(new Mixpanel.Context("Crash", object)).handle((unused, throwable) -> {
            if (throwable != null)
                System.err.printf("Failed to upload crash report! %s: %s%n", throwable.getClass().getSimpleName(), throwable.getMessage());
            return null;
        });
    }

    private static String getCauseAsString(Throwable cause) {
        try(var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
            cause.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            return "Failed to get cause: " + e.getMessage();
        }
    }
}
