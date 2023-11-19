package me.melontini.andromeda.client;

import lombok.Getter;
import me.melontini.andromeda.client.config.AutoConfigScreen;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.Support;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.Objects;

import static me.melontini.andromeda.util.CommonValues.MODID;

@Getter
@Environment(EnvType.CLIENT)
public class AndromedaClient {

    private static AndromedaClient INSTANCE;

    private final Notifier notifier = new Notifier();

    public static void init() {
        INSTANCE = new AndromedaClient();
        INSTANCE.onInitializeClient();
        FabricLoader.getInstance().getObjectShare().put("andromeda:client", INSTANCE);
    }

    public void onInitializeClient() {
        Support.run("cloth-config", () -> AutoConfigScreen::register);
        ClientSideNetworking.register();

        FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(MODID, "dark"), modContainer, ResourcePackActivationType.NORMAL);
        });

        Support.runWeak(EnvType.CLIENT, () -> AndromedaReporter::handleUpload);
    }

    public void lateInit() {
        notifier.showQueued();
    }

    @Override
    public String toString() {
        return "AndromedaClient{version=" + CommonValues.version() + "}";
    }

    public static AndromedaClient get() {
        return Objects.requireNonNull(INSTANCE, "AndromedaClient not initialized");
    }
}
