package me.melontini.andromeda.util;

import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CommonValues {

    public static final String MODID = "andromeda";

    private static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
    private static final String MOD_VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    private static final boolean MOD_UPDATED;

    private static final Path HIDDEN_PATH = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");

    private static final Platform PLATFORM;
    private static final EnvType ENVIRONMENT = FabricLoader.getInstance().getEnvironmentType();

    public static ModContainer mod() {
        return MOD_CONTAINER;
    }
    public static String version() {
        return MOD_VERSION;
    }
    public static boolean updated() {
        return MOD_UPDATED;
    }

    public static Path hiddenPath() {
        if (!Files.exists(HIDDEN_PATH)) {
            Utilities.runUnchecked(() -> Files.createDirectories(HIDDEN_PATH));
            try {
                if (HIDDEN_PATH.getFileSystem().supportedFileAttributeViews().contains("dos"))
                    Files.setAttribute(HIDDEN_PATH, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ignored) {
                AndromedaLog.warn("Failed to hide the .andromeda folder");
            }
        }
        return HIDDEN_PATH;
    }
    public static Path configPath() {
        return CONFIG_PATH;
    }

    public static Platform platform() {
        return PLATFORM;
    }
    public static EnvType environment() {
        return ENVIRONMENT;
    }

    static {
        PLATFORM = resolvePlatform();
        MOD_UPDATED = verifyHash();
        Utilities.runUnchecked(() -> Files.deleteIfExists(hiddenPath().resolve("last_version.txt")));
    }

    private static boolean verifyHash() {
        String s;
        Path file = Path.of(Utilities.supplyUnchecked(() -> CommonValues.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        if (Files.isDirectory(file)) {
            file = MOD_CONTAINER.findPath("fabric.mod.json").orElseThrow(() -> new NoSuchElementException("fabric.mod.json"));
        }
        try (var is = Files.newInputStream(file)) {
            byte[] read = is.readAllBytes();
            s = HexFormat.of().formatHex(Utilities.supplyUnchecked(() -> MessageDigest.getInstance("SHA-1")).digest(read));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path lh = hiddenPath().resolve("last_hash.txt");
        if (Files.exists(lh)) {
            String lhHash = Utilities.supplyUnchecked(() -> Files.readString(lh));
            if (!lhHash.equals(s)) {
                Utilities.runUnchecked(() -> Files.writeString(lh, s));
                return true;
            }
        } else {
            Utilities.runUnchecked(() -> Files.writeString(lh, s));
            return true;
        }
        return false;
    }

    private static Platform resolvePlatform() {
        if (FabricLoader.getInstance().isModLoaded("connectormod")) {
            try {
                //The above check should be fine, but just in case.
                Class.forName("dev.su5ed.sinytra.connector.mod.ConnectorLoader");
                return Platform.CONNECTOR;
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (FabricLoader.getInstance().isModLoaded("quilt_loader")) {
            String sn = MixinService.getService().getName().replaceAll("^Knot|^Launchwrapper|^ModLauncher|/", "");
            if ("quilt".equalsIgnoreCase(sn)) return Platform.QUILT;
        }
        return Platform.FABRIC;
    }

    public enum Platform {
        FABRIC("fabricloader"),
        CONNECTOR("connectormod"),
        QUILT("quilt_loader"),
        FORGE("forge"),
        NEOFORGE("neoforge") {
            @Override
            public String version() {
                return modVersion(this).orElse(modVersion(FORGE).orElse("0.0.0"));
            }
        };

        final String modId;

        Platform(String modId) {
            this.modId = modId;
        }

        static Optional<String> modVersion(Platform p) {
            return FabricLoader.getInstance().getModContainer(p.modId)
                    .map(container -> container.getMetadata().getVersion().getFriendlyString());
        }

        public String version() {
            return modVersion(this).orElse("0.0.0");
        }

        @Override
        public String toString() {
            return StringUtils.capitalize(name().toLowerCase());
        }
    }
}
