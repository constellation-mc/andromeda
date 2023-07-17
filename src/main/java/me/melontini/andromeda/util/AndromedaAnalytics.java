package me.melontini.andromeda.util;

import com.google.gson.Gson;
import me.melontini.andromeda.Andromeda;
import me.melontini.dark_matter.analytics.Analytics;
import me.melontini.dark_matter.analytics.Prop;
import me.melontini.dark_matter.analytics.mixpanel.MixpanelAnalytics;
import net.fabricmc.loader.api.FabricLoader;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;

public class AndromedaAnalytics {
    public static final String CRASH_UUID = "be4db047-16df-4e41-9121-f1e87618ddea";
    private static final MixpanelAnalytics.Handler HANDLER = MixpanelAnalytics.init(new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE=")), true);
    public static void handleUpload() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if (Andromeda.CONFIG.sendOptionalData) {
                HANDLER.send(messageBuilder -> {
                    JSONObject object = new JSONObject();
                    object.put("mod_version", Andromeda.MOD_VERSION);
                    object.put("mc_version", Prop.MINECRAFT_VERSION.get());
                    return messageBuilder.set(Analytics.getUUIDString(), object);
                });

                Gson gson = new Gson();
                Path fakeConfig = Andromeda.HIDDEN_PATH.resolve("config_copy.json");
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

    private static void stripNonBooleans(JSONObject object) {
        for (String s : new HashSet<>(object.keySet())) {
            try {
                stripNonBooleans(object.getJSONObject(s));
            } catch (Exception ignored) {
                try {
                    object.getBoolean(s);
                } catch (Exception ignored2) {
                    object.remove(s);
                }
            }
        }
    }

    private static void sendConfig(Gson gson) {
        HANDLER.send(messageBuilder -> {
            JSONObject object = new JSONObject();
            JSONObject config = new JSONObject(gson.toJson(Andromeda.CONFIG));
            stripNonBooleans(config);
            object.put("Config", config);
            return messageBuilder.set(Analytics.getUUIDString(), object);
        });
    }
}
