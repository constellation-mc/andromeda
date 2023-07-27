package me.melontini.andromeda.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.analytics.Analytics;
import me.melontini.dark_matter.analytics.Prop;
import me.melontini.dark_matter.analytics.crashes.Crashlytics;
import me.melontini.dark_matter.analytics.mixpanel.MixpanelAnalytics;
import me.melontini.dark_matter.util.Utilities;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.asm.service.MixinService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

public class AndromedaAnalytics {
    public static final String CRASH_UUID = "be4db047-16df-4e41-9121-f1e87618ddea";
    private static final MixpanelAnalytics.Handler HANDLER = MixpanelAnalytics.init(new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE=")), true);
    public static void handleUpload() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if (Andromeda.CONFIG.sendOptionalData) {
                HANDLER.send(messageBuilder -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("mod_version", SharedConstants.MOD_VERSION);
                    object.addProperty("mc_version", Prop.MINECRAFT_VERSION.get());
                    object.addProperty("modloader", Utilities.supply(() -> {
                        String sn = MixinService.getService().getName();
                        if (sn.contains("/Fabric")) return "Fabric";
                        else if (sn.contains("/Quilt")) return "Quilt";
                        else return "Other";
                    }));
                    AndromedaLog.info("Uploading optional data (Environment): \n" + object);
                    messageBuilder.set(Analytics.getUUIDString(), object);
                });

                Gson gson = new Gson();
                Path fakeConfig = SharedConstants.HIDDEN_PATH.resolve("config_copy.json");
                String currentConfig = gson.toJson(Andromeda.CONFIG);
                if (!Files.exists(fakeConfig)) {
                    try {
                        Files.createDirectories(fakeConfig.getParent());
                        Files.write(fakeConfig, currentConfig.getBytes());
                        sendConfig(gson);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String config = new String(Files.readAllBytes(fakeConfig));
                        if (!config.equals(currentConfig)) {
                            try {
                                Files.write(fakeConfig, currentConfig.getBytes());
                                sendConfig(gson);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                HANDLER.send(messageBuilder -> messageBuilder.delete(Analytics.getUUIDString()));
            }
        }
    }

    public static void registerCrashHandler() {
        Crashlytics.addHandler("andromeda", (report, cause, latestLog, envType) -> {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment() && Andromeda.CONFIG.sendCrashReports) {
                if (cause instanceof AndromedaException e && !e.shouldReport()) return false;
                String s = report.getCauseAsString();
                return s.contains("me.melontini.andromeda.");
            } else return false;
        }, (report, cause, latestLog, envType) -> HANDLER.send(messageBuilder -> {
            AndromedaLog.warn("Found Andromeda in trace, collecting and uploading crash report...");
            JsonObject object = new JsonObject();

            //fill trace.
            JsonArray stackTrace = new JsonArray();
            for (String string : report.getCauseAsString().lines().toList()) stackTrace.add(string);
            object.add("stackTrace", stackTrace);

            //fill loaded mods.
            JsonArray mods = new JsonArray();
            List<ModContainer> loadedMods = FabricLoader.getInstance().getAllMods().stream().sorted((a, b) -> a.getMetadata().getId().compareToIgnoreCase(b.getMetadata().getId())).filter(modContainer -> {
                String id = modContainer.getMetadata().getId();
                if (id.matches("fabric-[a-zA-Z_\\-]+-v\\d+")) return false;
                else if (id.matches("quilt_[a-zA-Z_\\-]+") || id.matches("quilted_fabric_[a-zA-Z_\\-]+_v\\d+")) return false;
                else if (id.startsWith("dark-matter-") && !id.equals("dark-matter-base")) return false;
                else if (id.matches("^org_jetbrains_kotlinx?_kotlinx?")) return false;
                else if (id.startsWith("cardinal-components-")) return false;
                else if (id.matches("terraform-[a-zA-Z_\\-]+-v\\d+")) return false;
                else if (id.matches("libjf-[a-zA-Z_\\-]+-v\\d+")) return false;
                return true;
            }).toList();
            for (ModContainer mod : loadedMods)
                mods.add(mod.getMetadata().getId() + " (" + mod.getMetadata().getVersion().getFriendlyString() + ")");
            object.add("mods", mods);

            object.addProperty("environment", envType.toString().toLowerCase());

            messageBuilder.trackEvent(CRASH_UUID, "Crash", object);
        }, true));
    }

    private static void stripNonBooleans(JsonObject object) {
        for (String s : new HashSet<>(object.keySet())) {
            if (object.get(s).isJsonObject()) {
                stripNonBooleans(object.getAsJsonObject(s));
            } else {
                if (object.get(s).isJsonPrimitive())
                    if (object.get(s).getAsJsonPrimitive().isBoolean()) continue;
                object.remove(s);
            }
        }
    }

    private static void sendConfig(Gson gson) {
        HANDLER.send(messageBuilder -> {
            JsonObject object = new JsonObject();
            JsonObject config = JsonParser.parseString(gson.toJson(Andromeda.CONFIG)).getAsJsonObject();
            stripNonBooleans(config);
            object.add("Config", config);
            AndromedaLog.info("Uploading optional data (Config)");
            messageBuilder.set(Analytics.getUUIDString(), object);
        });
    }
}
