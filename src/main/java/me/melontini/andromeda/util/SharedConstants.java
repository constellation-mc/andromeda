package me.melontini.andromeda.util;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class SharedConstants {
    public static final String MODID = "andromeda";
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    public static final Path HIDDEN_PATH = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
    public static final boolean FABRICATION_LOADED = FabricLoader.getInstance().isModLoaded("fabrication");
}
