package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.analytics.Analytics;
import me.melontini.dark_matter.api.analytics.mixpanel.MixpanelAnalytics;
import me.melontini.dark_matter.api.analytics.mixpanel.MixpanelHandler;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Set;

public class CrashHandler {

    public static final String CRASH_UUID = "be4db047-16df-4e41-9121-f1e87618ddea";
    private static final Analytics ANALYTICS = Analytics.get(CommonValues.mod());
    private static final MixpanelHandler HANDLER = Utilities.supply(() -> MixpanelAnalytics.init(ANALYTICS, new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE=")), true));
    private static volatile boolean mainHooked = false;

    @SuppressWarnings("deprecation")
    public static void nukeProfile() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if (CommonValues.updated() && !ANALYTICS.getDefaultUUID().equals(ANALYTICS.getUUID())) {
                Analytics.oldUUID().ifPresent(uuid -> HANDLER.send((mixpanel, analytics) -> mixpanel.delete(uuid.toString())));
                HANDLER.send((mixpanel, analytics) -> mixpanel.delete(analytics.getUUIDString()));
            }
        }
    }

    public static void tickMain() {
        mainHooked = true;
    }

    public static void accept(AndromedaException e) {
        if (e.shouldReport() && !mainHooked) {
            handleCrash(false, e, e.getMessage(), CommonValues.environment());
        }
    }

    public static Analytics get() {
        return ANALYTICS;
    }

    private static boolean findAndromedaInTrace(Throwable cause) {
        if (cause instanceof AndromedaException e && e.shouldReport()) return true;

        for (StackTraceElement element : cause.getStackTrace()) {
            if (element.isNativeMethod()) continue;
            String cls = element.getClassName();
            if (cls.contains("me.melontini.andromeda.")) return true;
            if (cls.contains("net.minecraft.")) {
                String mthd = element.getMethodName();
                return (mthd.contains("$andromeda$") || mthd.contains(".andromeda$"));
            }
        }
        return cause.getCause() != null && findAndromedaInTrace(cause.getCause());
    }

    public static void handleCrash(boolean force, Throwable cause, String message, EnvType envType) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() || !Config.get().sendCrashReports) return;
        if (!force && !findAndromedaInTrace(cause)) return;

        HANDLER.send((mixpanel, analytics) -> {
            AndromedaLog.warn("Found Andromeda in trace, collecting and uploading crash report...");
            JsonObject object = new JsonObject();

            //fill trace.
            JsonArray stackTrace = new JsonArray();
            for (String string : getCauseAsString(cause, message).lines().toList()) stackTrace.add(string);
            object.add("stackTrace", stackTrace);

            object.addProperty("environment", envType.toString().toLowerCase());
            object.addProperty("platform", CommonValues.platform().toString().toLowerCase());

            JsonArray mods = new JsonArray();
            Set<String> importantMods = Sets.newHashSet("andromeda", "minecraft", "modmenu", "dark-matter-base", "fabric-api", "fabricloader", "cloth-config", "cloth_config", "connectormod", "forge", "iceberg");
            CauseFinder.findCause(cause).ifPresent(importantMods::add);

            for (String importantMod : importantMods) {
                FabricLoader.getInstance().getModContainer(importantMod).ifPresent(mod -> mods.add(importantMod + " (" + mod.getMetadata().getVersion().getFriendlyString() + ")"));
            }

            object.add("mods", mods);

            mixpanel.trackEvent(CRASH_UUID, "Crash", object);
        });
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
