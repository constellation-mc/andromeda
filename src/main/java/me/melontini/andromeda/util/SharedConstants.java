package me.melontini.andromeda.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SharedConstants {

    public static final String MODID = "andromeda";

    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
    public static final String MOD_VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    public static final boolean MOD_UPDATED;

    public static final Path HIDDEN_PATH = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");

    public static final boolean FABRICATION_LOADED = FabricLoader.getInstance().isModLoaded("fabrication");

    public static final Platform PLATFORM;


    static {
        PLATFORM = getPlatform();
        MOD_UPDATED = checkModUpdate();
    }

    private static Platform getPlatform() {
        if (FabricLoader.getInstance().isModLoaded("connectormod")) {
            try {
                //The above check should be fine, but just in case.
                Class.forName("dev.su5ed.sinytra.connector.mod.ConnectorLoader");
                return Platform.CONNECTOR;
            } catch (ClassNotFoundException ignored) {};
        }
        if (FabricLoader.getInstance().isModLoaded("quilt_loader")) {
            String sn = MixinService.getService().getName().replaceAll("^Knot|^Launchwrapper|^ModLauncher|/", "");
            if ("quilt".equalsIgnoreCase(sn)) return Platform.QUILT;
        }
        return Platform.FABRIC;
    }

    private static boolean checkModUpdate() {
        Path lastVer = SharedConstants.HIDDEN_PATH.resolve("last_version.txt");
        boolean modUpdated = true;
        if (Files.exists(lastVer)) {
            try {
                Version lastVersion = Version.parse(Files.readString(lastVer));
                if (lastVersion.compareTo(MOD_CONTAINER.getMetadata().getVersion()) <= 0) {
                    modUpdated = false;
                }
            } catch (VersionParsingException | IOException ignored) {}
        }
        writeVersion(lastVer);
        return modUpdated;
    }

    private static void writeVersion(Path lastVer) {
        try {
            if (!Files.exists(lastVer.getParent())) Files.createDirectories(lastVer.getParent());
            Files.writeString(lastVer, MOD_VERSION);
        } catch (IOException ignored) {}
    }

    public enum Platform {
        FABRIC,
        CONNECTOR,
        QUILT,
        FORGE,
        NEOFORGED
    }
}
