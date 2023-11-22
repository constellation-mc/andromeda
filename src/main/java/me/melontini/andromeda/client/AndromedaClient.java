package me.melontini.andromeda.client;

import lombok.Getter;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.client.config.AutoConfigScreen;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.Support;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Objects;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.util.CommonValues.MODID;

@Getter
@Environment(EnvType.CLIENT)
public class AndromedaClient {

    private static AndromedaClient INSTANCE;

    public static void init() {
        INSTANCE = new AndromedaClient();
        INSTANCE.onInitializeClient();
        FabricLoader.getInstance().getObjectShare().put("andromeda:client", INSTANCE);
    }

    public void onInitializeClient() {
        Support.run("cloth-config", () -> AutoConfigScreen::register);
        if (!Config.get().sideOnlyMode) ClientSideNetworking.register();

        FabricLoader.getInstance().getModContainer(MODID).ifPresent(mod ->
                ResourceManagerHelper.registerBuiltinResourcePack(id("dark"), mod, ResourcePackActivationType.NORMAL));

        Support.runWeak(EnvType.CLIENT, () -> AndromedaReporter::handleUpload);
    }

    public void lateInit() {
    }

    @Override
    public String toString() {
        return "AndromedaClient{version=" + CommonValues.version() + "}";
    }

    public static AndromedaClient get() {
        return Objects.requireNonNull(INSTANCE, "AndromedaClient not initialized");
    }
}
