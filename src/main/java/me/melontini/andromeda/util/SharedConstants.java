package me.melontini.andromeda.util;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class SharedConstants {
    public static final String MODID = "andromeda";
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    public static final Path HIDDEN_PATH = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");
    public static final boolean FABRICATION_LOADED = FabricLoader.getInstance().isModLoaded("fabrication");
    public static final boolean CONNECTOR_LOADED = FabricLoader.getInstance().isModLoaded("connectormod");
    public static final Platform PLATFORM = CONNECTOR_LOADED ? Platform.CONNECTOR : Platform.FABRIC;

    public enum Platform {
        FABRIC,
        CONNECTOR
    }
}
