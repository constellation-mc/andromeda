package me.melontini.andromeda.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.analytics.Analytics;
import me.melontini.dark_matter.api.analytics.Prop;
import me.melontini.dark_matter.api.analytics.crashes.Crashlytics;
import me.melontini.dark_matter.api.analytics.mixpanel.MixpanelAnalytics;
import me.melontini.dark_matter.api.analytics.mixpanel.MixpanelHandler;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.service.MixinService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class AndromedaAnalytics {
    public static final String CRASH_UUID = "be4db047-16df-4e41-9121-f1e87618ddea";
    private static final MixpanelHandler HANDLER = MixpanelAnalytics.init(new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE=")), true);

    public static void handleUpload() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if (Andromeda.CONFIG.sendOptionalData) {
                if (SharedConstants.MOD_UPDATED) {
                    HANDLER.send(messageBuilder -> {
                        JsonObject object = new JsonObject();
                        object.addProperty("mod_version", SharedConstants.MOD_VERSION.split("-")[0]);
                        object.addProperty("mc_version", Prop.MINECRAFT_VERSION.get());
                        object.addProperty("modloader", Utilities.supply(() -> {
                            String sn = MixinService.getService().getName().replaceAll("^Knot|^Launchwrapper|^ModLauncher|/", "");
                            if (sn.isEmpty()) return "Other";
                            return sn;
                        }));
                        AndromedaLog.info("Uploading optional data (Environment): \n" + object);
                        messageBuilder.set(Analytics.getUUIDString(), object);
                    });
                } else AndromedaLog.info("Skipped optional data upload (Environment)");

                Path fakeConfig = SharedConstants.HIDDEN_PATH.resolve("config_copy.json");
                try {
                    Files.deleteIfExists(fakeConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                HANDLER.send(messageBuilder -> messageBuilder.delete(Analytics.getUUIDString()));
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

    public static void registerCrashHandler() {
        Crashlytics.addHandler("andromeda", (report, cause, latestLog, envType) -> {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment() && Andromeda.CONFIG.sendCrashReports) {
                if (cause instanceof AndromedaException e) return e.shouldReport();
                return findAndromedaInTrace(cause);
            } else return false;
        }, (report, cause, latestLog, envType) -> HANDLER.send(messageBuilder -> {
            AndromedaLog.warn("Found Andromeda in trace, collecting and uploading crash report...");
            JsonObject object = new JsonObject();

            //fill trace.
            JsonArray stackTrace = new JsonArray();
            for (String string : report.getCauseAsString().lines().toList()) stackTrace.add(string);
            object.add("stackTrace", stackTrace);

            object.addProperty("environment", envType.toString().toLowerCase());
            object.addProperty("platform", SharedConstants.PLATFORM.toString().toLowerCase());

            JsonArray mods = new JsonArray();
            String[] importantMods = new String[]{"andromeda", "minecraft", "modmenu", "dark-matter-base", "fabric-api", "fabricloader", "cloth-config", "cloth_config", "connectormod", "forge", "iceberg"};
            for (String importantMod : importantMods) {
                FabricLoader.getInstance().getModContainer(importantMod).ifPresent(mod -> mods.add(importantMod + " (" + mod.getMetadata().getVersion().getFriendlyString() + ")"));
            }
            object.add("mods", mods);

            messageBuilder.trackEvent(CRASH_UUID, "Crash", object);
        }, true));
    }
}
