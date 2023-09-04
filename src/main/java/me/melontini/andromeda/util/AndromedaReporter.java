package me.melontini.andromeda.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.analytics.Analytics;
import me.melontini.dark_matter.api.analytics.Prop;
import me.melontini.dark_matter.api.analytics.crashes.Crashlytics;
import me.melontini.dark_matter.api.analytics.mixpanel.MixpanelAnalytics;
import me.melontini.dark_matter.api.analytics.mixpanel.MixpanelHandler;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class AndromedaReporter {

    public static final String CRASH_UUID = "be4db047-16df-4e41-9121-f1e87618ddea";
    private static final Analytics ANALYTICS = Analytics.get(SharedConstants.MOD_CONTAINER);
    private static final MixpanelHandler HANDLER = Utilities.supply(() -> {
        final Analytics analytics = Analytics.get(SharedConstants.MOD_CONTAINER);
        Crashlytics.addHandler("andromeda", ANALYTICS, (report, cause, latestLog, envType) -> handleCrash(cause, report.getMessage(), envType));
        return MixpanelAnalytics.init(analytics, new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE=")), true);
    });

    @SuppressWarnings("deprecation")
    public static void handleUpload() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Analytics.oldUUID().ifPresent(uuid -> HANDLER.send((mixpanel, analytics) -> mixpanel.delete(uuid.toString())));
            if (Config.get().sendOptionalData) {
                if (SharedConstants.MOD_UPDATED) {
                    HANDLER.send((mixpanel, analytics) -> {
                        JsonObject object = new JsonObject();
                        object.addProperty("mod_version", SharedConstants.MOD_VERSION.split("-")[0]);
                        object.addProperty("mc_version", Prop.MINECRAFT_VERSION.get());
                        object.addProperty("modloader", StringUtils.capitalize(SharedConstants.PLATFORM.name().toLowerCase()));
                        AndromedaLog.info("Uploading optional data.: " + object);
                        mixpanel.set(analytics.getUUIDString(), object);
                    });
                } else AndromedaLog.info("Skipped optional data upload.");

                Path fakeConfig = SharedConstants.HIDDEN_PATH.resolve("config_copy.json");
                try {
                    Files.deleteIfExists(fakeConfig);
                } catch (Exception e) {
                    AndromedaLog.warn("Failed to delete config_copy.json", e);
                }
            } else {
                HANDLER.send((mixpanel, analytics) -> mixpanel.delete(analytics.getUUIDString()));
            }
        }
    }

    private static boolean findAndromedaInTrace(Throwable cause) {
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

    public static void handleCrash(Throwable cause, String message, EnvType envType) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() || !Config.get().sendCrashReports) return;
        if (cause instanceof AndromedaException e && !e.shouldReport()) return;
        if (!findAndromedaInTrace(cause)) return;

        HANDLER.send((mixpanel, analytics) -> {
            AndromedaLog.warn("Found Andromeda in trace, collecting and uploading crash report...");
            JsonObject object = new JsonObject();

            //fill trace.
            JsonArray stackTrace = new JsonArray();
            for (String string : getCauseAsString(cause, message).lines().toList()) stackTrace.add(string);
            object.add("stackTrace", stackTrace);

            object.addProperty("environment", envType.toString().toLowerCase());
            object.addProperty("platform", SharedConstants.PLATFORM.toString().toLowerCase());

            JsonArray mods = new JsonArray();
            String[] importantMods = new String[]{"andromeda", "minecraft", "modmenu", "dark-matter-base", "fabric-api", "fabricloader", "cloth-config", "cloth_config", "connectormod", "forge", "iceberg"};
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

    public static void init() {
    }
}
