package me.melontini.andromeda.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SharedConstants {
    public static final String MODID = "andromeda";
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
    public static final String MOD_VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    public static final Path HIDDEN_PATH = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");
    public static final boolean FABRICATION_LOADED = FabricLoader.getInstance().isModLoaded("fabrication");
    public static final boolean CONNECTOR_LOADED = FabricLoader.getInstance().isModLoaded("connectormod");
    public static final Platform PLATFORM = CONNECTOR_LOADED ? Platform.CONNECTOR : Platform.FABRIC;
    public static final boolean MOD_UPDATED;

    static {
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
        MOD_UPDATED = modUpdated;
    }

    private static void writeVersion(Path lastVer) {
        try {
            if (!Files.exists(lastVer.getParent())) Files.createDirectories(lastVer.getParent());
            Files.writeString(lastVer, MOD_VERSION);
        } catch (IOException ignored) {}
    }

    public enum Platform {
        FABRIC,
        CONNECTOR
    }
}
